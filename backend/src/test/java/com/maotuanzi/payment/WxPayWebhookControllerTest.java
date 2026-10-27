package com.maotuanzi.payment;

import com.alibaba.fastjson2.JSONObject;
import com.maotuanzi.config.PaymentConfig;
import com.maotuanzi.controller.webhook.WxPayWebhookController;
import com.maotuanzi.entity.Order;
import com.maotuanzi.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.time.Instant;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WxPayWebhookControllerTest {

    private static final String API_KEY = "01234567890123456789012345678901";
    private static final String SERIAL = "A1B2C3D4";

    @TempDir
    Path tempDir;

    @Test
    void rejectsPlaintextCallbackWhenVerificationCredentialsAreMissing() {
        OrderService orderService = mock(OrderService.class);
        WxPayWebhookController controller = new WxPayWebhookController(orderService, new PaymentConfig());

        ResponseEntity<String> response = controller.wxPayCallback(
                "{\"out_trade_no\":\"order-1\",\"transaction_id\":\"forged\"}", null, null, null, null);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        verify(orderService, never()).markPaid(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void acceptsOnlySignedAndDecryptedSuccessNotificationMatchingOrderAmount() throws Exception {
        PaymentConfig config = configuredPayment();
        OrderService orderService = mock(OrderService.class);
        Order order = new Order();
        order.setOrderNo("order-1");
        order.setPayAmountCents(1990);
        order.setChannel("WECHAT");
        when(orderService.detailByNo("order-1")).thenReturn(order);
        WxPayWebhookController controller = new WxPayWebhookController(orderService, config);

        String body = successfulNotification(1990);
        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        String nonce = "nonce-123";
        String signed = sign(body, timestamp, nonce);

        ResponseEntity<String> response = controller.wxPayCallback(body, timestamp, nonce, SERIAL, signed);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(orderService).markPaid("order-1", "wx-txn-1");
    }

    @Test
    void rejectsInvalidSignatureWithoutReadingOrChangingOrder() throws Exception {
        PaymentConfig config = configuredPayment();
        OrderService orderService = mock(OrderService.class);
        WxPayWebhookController controller = new WxPayWebhookController(orderService, config);
        String body = successfulNotification(1990);

        ResponseEntity<String> response = controller.wxPayCallback(
                body, String.valueOf(Instant.now().getEpochSecond()), "nonce-123", SERIAL, "invalid-signature");

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(orderService, never()).detailByNo(org.mockito.ArgumentMatchers.anyString());
        verify(orderService, never()).markPaid(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void rejectsAmountMismatchWithoutGrantingEntitlement() throws Exception {
        PaymentConfig config = configuredPayment();
        OrderService orderService = mock(OrderService.class);
        Order order = new Order();
        order.setOrderNo("order-1");
        order.setPayAmountCents(1990);
        order.setChannel("WECHAT");
        when(orderService.detailByNo("order-1")).thenReturn(order);
        WxPayWebhookController controller = new WxPayWebhookController(orderService, config);
        String body = successfulNotification(1);
        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        String nonce = "nonce-123";

        ResponseEntity<String> response = controller.wxPayCallback(body, timestamp, nonce, SERIAL, sign(body, timestamp, nonce));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(orderService, never()).markPaid(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString());
    }

    private PaymentConfig configuredPayment() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        Path publicKeyPath = tempDir.resolve("platform-public-key.pem");
        String pem = "-----BEGIN PUBLIC KEY-----\n" + Base64.getMimeEncoder(64, new byte[]{'\n'})
                .encodeToString(keyPair.getPublic().getEncoded()) + "\n-----END PUBLIC KEY-----\n";
        Files.writeString(publicKeyPath, pem, StandardCharsets.UTF_8);
        this.signingKeyPair = keyPair;

        PaymentConfig config = new PaymentConfig();
        ReflectionTestUtils.setField(config, "wechatApiKey", API_KEY);
        ReflectionTestUtils.setField(config, "wechatAppId", "wx-app");
        ReflectionTestUtils.setField(config, "wechatMchId", "mch-1");
        ReflectionTestUtils.setField(config, "wechatPlatformPublicKeyPath", publicKeyPath.toString());
        ReflectionTestUtils.setField(config, "wechatPlatformSerialNo", SERIAL.toLowerCase());
        return config;
    }

    private KeyPair signingKeyPair;

    private String successfulNotification(int totalCents) throws Exception {
        JSONObject plain = new JSONObject();
        plain.put("appid", "wx-app");
        plain.put("mchid", "mch-1");
        plain.put("out_trade_no", "order-1");
        plain.put("transaction_id", "wx-txn-1");
        plain.put("trade_state", "SUCCESS");
        JSONObject amount = new JSONObject();
        amount.put("total", totalCents);
        amount.put("currency", "CNY");
        plain.put("amount", amount);

        byte[] nonce = "123456789012".getBytes(StandardCharsets.UTF_8);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(API_KEY.getBytes(StandardCharsets.UTF_8), "AES"),
                new GCMParameterSpec(128, nonce));
        byte[] encrypted = cipher.doFinal(plain.toJSONString().getBytes(StandardCharsets.UTF_8));
        JSONObject resource = new JSONObject();
        resource.put("nonce", new String(nonce, StandardCharsets.UTF_8));
        resource.put("ciphertext", Base64.getEncoder().encodeToString(encrypted));
        JSONObject envelope = new JSONObject();
        envelope.put("resource", resource);
        return envelope.toJSONString();
    }

    private String sign(String body, String timestamp, String nonce) throws Exception {
        Signature signer = Signature.getInstance("SHA256withRSA");
        signer.initSign(signingKeyPair.getPrivate());
        signer.update((timestamp + "\n" + nonce + "\n" + body + "\n").getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(signer.sign());
    }
}
