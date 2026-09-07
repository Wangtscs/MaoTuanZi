package com.maotuanzi.service.impl;

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
 * 华为 IAP 渠道（后端侧）。
 *
 * <p>后端负责为前端拉起华为 IAP 组装参数（productId / developerPayload / signature / signedData / iapApiVersion），
 * 并预留华为服务端校验 REST 调用位（校验购买凭证）。凭证缺失时返回占位（mock）参数。</p>
 *
 * <p>注意：华为 IAP 的后端服务端校验走华为 REST（非 Maven SDK），无需引入额外依赖；
 * 真实校验逻辑见 {@link #verifyWithHuaweiServer(Order)}。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HuaweiIapChannel implements PaymentChannel {

    private final PaymentConfig paymentConfig;

    @Override
    public UnifiedOrderResult unifiedOrder(Order order, String channel) {
        UnifiedOrderResult result = new UnifiedOrderResult();
        result.setChannel("HUAWEI");
        result.setExpireAt(order.getExpireAt() == null ? null
                : order.getExpireAt().toInstant(ZoneOffset.UTC).toEpochMilli());

        try {
            if (paymentConfig.hasCredentials("HUAWEI")) {
                // 真实模式：组装 IAP 参数，并预留华为服务端校验
                verifyWithHuaweiServer(order);
            }
            result.setIapParams(buildIapParams(order));
        } catch (Exception e) {
            log.warn("华为 IAP 下单异常，降级为 mock 模式 orderNo={} err={}", order.getOrderNo(), e.getMessage());
            result.setIapParams(buildIapParams(order));
        }
        return result;
    }

    /**
     * 组装华为 IAP 拉起参数（严格对齐前端契约字段）。
     */
    private JSONObject buildIapParams(Order order) {
        JSONObject p = new JSONObject();
        p.put("productId", order.getProductId());
        p.put("developerPayload", order.getOrderNo());
        // 占位：真实环境由华为服务端签发 signature / signedData
        p.put("signature", "mock_signature");
        p.put("signedData", "mock_signed_data");
        p.put("iapApiVersion", "1.0.0");
        return p;
    }

    /**
     * 预留：调用华为服务端校验购买凭证（通过华为 REST，使用 clientId/key 换取 access token 后校验）。
     * 当前为占位实现，不影响主流程。
     */
    private void verifyWithHuaweiServer(Order order) {
        // TODO(商业接入): 以 huawei.merchantId + huawei.key 获取 access token，调用华为 IAP 服务端校验接口。
        log.info("华为 IAP 服务端校验（预留） orderNo={}", order.getOrderNo());
    }
}
