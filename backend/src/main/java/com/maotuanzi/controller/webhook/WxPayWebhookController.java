package com.maotuanzi.controller.webhook;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.maotuanzi.common.exception.BizException;
import com.maotuanzi.config.PaymentConfig;
import com.maotuanzi.entity.Order;
import com.maotuanzi.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 微信支付 v3 回调。只有签名通过且解密字段与本地订单匹配时才入账。
 */
@Slf4j
@RestController
@RequestMapping("/v1/webhooks")
@RequiredArgsConstructor
public class WxPayWebhookController {

    private final OrderService orderService;
    private final PaymentConfig paymentConfig;

    private static final long MAX_CALLBACK_AGE_SECONDS = 300;
    private static final Pattern PUBLIC_KEY_PEM_MARKERS = Pattern.compile("-----BEGIN PUBLIC KEY-----|-----END PUBLIC KEY-----|\\s");

    @PostMapping("/wx-pay")
    public ResponseEntity<String> wxPayCallback(
            @RequestBody String rawBody,
            @RequestHeader(value = "Wechatpay-Timestamp", required = false) String timestamp,
            @RequestHeader(value = "Wechatpay-Nonce", required = false) String nonce,
            @RequestHeader(value = "Wechatpay-Serial", required = false) String serial,
            @RequestHeader(value = "Wechatpay-Signature", required = false) String signature) {
        try {
            if (!paymentConfig.hasWechatCallbackCredentials()) {
                log.error("微信支付回调验签配置缺失，拒绝处理");
                return failure(HttpStatus.SERVICE_UNAVAILABLE);
            }

            if (!verifySignature(rawBody, timestamp, nonce, serial, signature)) {
                log.warn("微信支付回调签名无效或已过期，拒绝处理");
                return failure(HttpStatus.UNAUTHORIZED);
            }

            JSONObject envelope = JSON.parseObject(rawBody);
            JSONObject resource = envelope == null ? null : envelope.getJSONObject("resource");
            if (resource == null) {
                log.warn("微信支付 v3 回调缺少加密 resource");
                return failure(HttpStatus.BAD_REQUEST);
            }

            JSONObject plain = decryptResource(resource, paymentConfig.getWechatApiKey());
            String tradeState = plain.getString("trade_state");
            if (!"SUCCESS".equals(tradeState)) {
                // A valid notification for a closed/failed payment is acknowledged but never booked.
                return ResponseEntity.ok(success());
            }

            String outTradeNo = plain.getString("out_trade_no");
            String transactionId = plain.getString("transaction_id");
            if (!hasText(outTradeNo) || !hasText(transactionId)) {
                log.warn("微信支付成功回调缺少订单号或交易号");
                return failure(HttpStatus.BAD_REQUEST);
            }
            if (!paymentConfig.getWechatAppId().equals(plain.getString("appid"))
                    || !paymentConfig.getWechatMchId().equals(plain.getString("mchid"))) {
                log.warn("微信支付回调 appid/mchid 与当前商户配置不匹配");
                return failure(HttpStatus.BAD_REQUEST);
            }

            JSONObject amount = plain.getJSONObject("amount");
            Integer paidAmount = amount == null ? null : amount.getInteger("total");
            String currency = amount == null ? null : amount.getString("currency");
            if (paidAmount == null || paidAmount <= 0 || !"CNY".equals(currency)) {
                log.warn("微信支付回调金额字段无效");
                return failure(HttpStatus.BAD_REQUEST);
            }

            Order order = orderService.detailByNo(outTradeNo);
            if (order == null || order.getPayAmountCents() == null
                    || !paidAmount.equals(order.getPayAmountCents())
                    || !"WECHAT".equalsIgnoreCase(order.getChannel())) {
                log.warn("微信支付回调与本地订单金额或支付渠道不匹配");
                return failure(HttpStatus.BAD_REQUEST);
            }

            // markPaid is idempotent; all external payment facts have been authenticated above.
            orderService.markPaid(outTradeNo, transactionId);
            return ResponseEntity.ok(success());
        } catch (BizException e) {
            log.warn("微信支付回调业务异常 code={}", e.getCode());
            return failure(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("微信支付回调处理异常，未入账", e);
            return failure(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/wx-refund")
    public String wxRefundCallback(@RequestBody Map<String, Object> body) {
        log.info("收到微信退款回调 body={}", body);
        // 纯桩：真实场景需验签 + 解密后更新退款状态（预留）
        return success();
    }

    /**
     * 解密微信支付 v3 回调密文（AEAD_AES_256_GCM）。
     */
    private JSONObject decryptResource(Map<String, Object> resource, String apiKey) throws Exception {
        String cipherText = (String) resource.get("ciphertext");
        String nonce = (String) resource.get("nonce");
        String associated = (String) resource.get("associated_data");

        byte[] key = apiKey.getBytes(StandardCharsets.UTF_8);
        // WeChat Pay v3 nonce is UTF-8 text; only ciphertext is Base64 encoded.
        byte[] nonceBytes = nonce.getBytes(StandardCharsets.UTF_8);
        byte[] data = Base64.getDecoder().decode(cipherText);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(128, nonceBytes));
        if (associated != null) {
            cipher.updateAAD(associated.getBytes(StandardCharsets.UTF_8));
        }
        byte[] plain = cipher.doFinal(data);
        return JSON.parseObject(new String(plain, StandardCharsets.UTF_8));
    }

    private boolean verifySignature(String rawBody, String timestamp, String nonce, String serial, String signatureText) {
        try {
            if (!hasText(rawBody) || !hasText(timestamp) || !hasText(nonce) || !hasText(serial) || !hasText(signatureText)) {
                return false;
            }
            long timestampSeconds = Long.parseLong(timestamp);
            long nowSeconds = Instant.now().getEpochSecond();
            if (timestampSeconds < nowSeconds - MAX_CALLBACK_AGE_SECONDS
                    || timestampSeconds > nowSeconds + MAX_CALLBACK_AGE_SECONDS) {
                return false;
            }
            if (!normalizeSerial(paymentConfig.getWechatPlatformSerialNo()).equals(normalizeSerial(serial))) {
                return false;
            }

            String pem = Files.readString(Path.of(paymentConfig.getWechatPlatformPublicKeyPath()), StandardCharsets.UTF_8);
            String encoded = PUBLIC_KEY_PEM_MARKERS.matcher(pem).replaceAll("");
            byte[] keyBytes = Base64.getDecoder().decode(encoded);
            PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(keyBytes));
            Signature verifier = Signature.getInstance("SHA256withRSA");
            verifier.initVerify(publicKey);
            verifier.update((timestamp + "\n" + nonce + "\n" + rawBody + "\n").getBytes(StandardCharsets.UTF_8));
            return verifier.verify(Base64.getDecoder().decode(signatureText));
        } catch (Exception e) {
            log.warn("微信支付回调验签失败");
            return false;
        }
    }

    private String normalizeSerial(String serial) {
        return serial == null ? "" : serial.replace(":", "").replace(" ", "").toUpperCase();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String success() {
        return "{\"code\":\"SUCCESS\",\"message\":\"成功\"}";
    }

    private ResponseEntity<String> failure(HttpStatus status) {
        return ResponseEntity.status(status).body("{\"code\":\"FAIL\",\"message\":\"验签失败或回调数据无效\"}");
    }
}
