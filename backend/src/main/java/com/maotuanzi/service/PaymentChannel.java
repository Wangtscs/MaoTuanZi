package com.maotuanzi.service;

import com.maotuanzi.dto.UnifiedOrderResult;
import com.maotuanzi.entity.Order;

/**
 * 支付渠道统一下单接口。
 *
 * <p>各渠道实现（微信 / 华为）在凭证缺失或调用异常时，返回填充了占位（mock）参数的
 * {@link UnifiedOrderResult}，保证主流程不被打断、不依赖缺失的商户密钥。</p>
 */
public interface PaymentChannel {

    /**
     * 统一下单。
     *
     * @param order   待支付订单（实现可回填 {@code wxPrepayId} 等字段）
     * @param channel 渠道名（与实现对应的 WECHAT / HUAWEI）
     * @return 统一下单结果（含各端支付参数 / 到期时间）
     */
    UnifiedOrderResult unifiedOrder(Order order, String channel);
}
