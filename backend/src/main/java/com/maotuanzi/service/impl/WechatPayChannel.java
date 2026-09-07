package com.maotuanzi.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson2.JSONObject;
import com.maotuanzi.config.PaymentConfig;
import com.maotuanzi.dto.UnifiedOrderResult;
import com.maotuanzi.entity.Order;
import com.maotuanzi.service.PaymentChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;

/**
 * 微信支付渠道。
 *
 * <p><b>核心策略：mock 降级优先。</b> 当 {@link PaymentConfig#hasCredentials(String)} 为 false
 * （缺商户密钥）或真实下单调用出现异常时，返回伪造 JSAPI 预支付参数（占位值），
 * 保证业务流程不被打断，且绝不依赖缺失的商户密钥。</p>
 *
 * <p>真实路径（凭证齐全时）预留了对接微信支付 v3 JSAPI 下单的调用位；因本项目运行环境未配置
 * 商户私钥，统一走 mock 降级。详见 {@link #callWxUnifiedOrder(Order)}。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WechatPayChannel implements PaymentChannel {

    private final PaymentConfig paymentConfig;

    @Override
    public UnifiedOrderResult unifiedOrder(Order order, String channel) {
        UnifiedOrderResult result = new UnifiedOrderResult();
        result.setChannel("WECHAT");
        result.setExpireAt(order.getExpireAt() == null ? null
                : order.getExpireAt().toInstant(ZoneOffset.UTC).toEpochMilli());

        String prepayId = "mock_prepay_" + RandomUtil.randomString(20);
        try {
            if (!paymentConfig.hasCredentials("WECHAT")) {
                result.setWxPayParams(buildMockWxPayParams(prepayId));
                order.setWxPrepayId(prepayId);
                return result;
            }
            // 真实模式：调用微信 v3 JSAPI 下单（best-effort，异常则降级）
            JSONObject real = callWxUnifiedOrder(order);
            result.setWxPayParams(real);
            order.setWxPrepayId(real.getString("prepay_id"));
        } catch (Exception e) {
            log.warn("微信支付下单异常，降级为 mock 模式 orderNo={} err={}", order.getOrderNo(), e.getMessage());
            result.setWxPayParams(buildMockWxPayParams(prepayId));
            order.setWxPrepayId(prepayId);
        }
        return result;
    }

    /**
     * 真实微信 v3 JSAPI 统一下单（预留）。
     *
     * <p>完整接入需要：商户私钥（cert-path）+ 证书序列号 + APIv3 密钥做请求签名与回调解密。
     * 当前运行环境未配置这些密钥，故直接抛出，由 {@link #unifiedOrder} 捕获后降级为 mock。</p>
     */
    private JSONObject callWxUnifiedOrder(Order order) {
        // TODO(商业接入): 使用 wechatpay-java SDK 的 JsapiService 调 /v3/pay/transactions/jsapi，
        //   以商户私钥构造 RSAConfig，发起下单并将返回的 prepay_id 回填到 result。
        throw new IllegalStateException(
                "未配置微信商户私钥，无法发起真实下单；已自动降级为 mock 模式");
    }

    /**
     * 构造 mock 微信 JSAPI 调起参数（严格对齐前端契约字段，不返回额外 prepayId）。
     */
    private JSONObject buildMockWxPayParams(String prepayId) {
        long ts = System.currentTimeMillis() / 1000;
        String nonce = RandomUtil.randomString(16);
        JSONObject p = new JSONObject();
        p.put("appId", paymentConfig.getWechatAppId() == null ? "wx_mock" : paymentConfig.getWechatAppId());
        p.put("timeStamp", String.valueOf(ts));
        p.put("nonceStr", nonce);
        p.put("package", "prepay_id=" + prepayId);
        p.put("signType", "RSA");
        p.put("paySign", "mock_signature");
        return p;
    }
}
