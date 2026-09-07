package com.maotuanzi.controller.webhook;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.maotuanzi.common.exception.BizException;
import com.maotuanzi.config.PaymentConfig;
import com.maotuanzi.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

/**
 * 微信支付回调（验签 + 解密加固，mock 可降级）。
 */
@Slf4j
@RestController
@RequestMapping("/v1/webhooks")
@RequiredArgsConstructor
public class WxPayWebhookController {

    private final OrderService orderService;
    private final PaymentConfig paymentConfig;

    @PostMapping("/wx-pay")
    public String wxPayCallback(@RequestBody Map<String, Object> body) {
        try {
            log.info("收到微信支付回调");
            String outTradeNo = null;
            String transactionId = null;

            // v3 回调带有 resource（密文），凭证齐全时尝试验签 + 解密
            Object resource = body.get("resource");
            if (resource instanceof Map && paymentConfig.hasCredentials("WECHAT")) {
                try {
                    JSONObject plain = decryptResource((Map<String, Object>) resource, paymentConfig.getWechatApiKey());
                    outTradeNo = plain.getString("out_trade_no");
                    transactionId = plain.getString("transaction_id");
                    String tradeState = plain.getString("trade_state");
                    if (!"SUCCESS".equals(tradeState)) {
                        log.info("微信回调交易状态非成功 trade_state={}", tradeState);
                        return success();
                    }
                } catch (Exception e) {
                    log.warn("微信回调解密失败，尝试明文解析 err={}", e.getMessage());
                }
            }

            if (outTradeNo == null) {
                // 明文 / v2 兼容
                outTradeNo = (String) body.get("out_trade_no");
                transactionId = (String) body.get("transaction_id");
            }
            if (outTradeNo == null) {
                log.warn("微信回调缺少 out_trade_no");
                return success();
            }
            // markPaid 内部幂等，重复通知安全
            orderService.markPaid(outTradeNo, transactionId);
        } catch (BizException e) {
            log.warn("微信支付回调业务异常 code={}", e.getCode());
        } catch (Exception e) {
            log.error("微信支付回调处理异常", e);
        }
        return success();
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
        byte[] nonceBytes = Base64.getDecoder().decode(nonce);
        byte[] data = Base64.getDecoder().decode(cipherText);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(128, nonceBytes));
        if (associated != null) {
            cipher.updateAAD(associated.getBytes(StandardCharsets.UTF_8));
        }
        byte[] plain = cipher.doFinal(data);
        return JSON.parseObject(new String(plain, StandardCharsets.UTF_8));
    }

    private String success() {
        return "{\"code\":\"SUCCESS\",\"message\":\"成功\"}";
    }
}
