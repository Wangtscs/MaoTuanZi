package com.maotuanzi.service;

import com.maotuanzi.common.result.PageResult;
import com.maotuanzi.dto.OrderCreateRequest;
import com.maotuanzi.dto.RefundResult;
import com.maotuanzi.dto.UnifiedOrderResult;
import com.maotuanzi.entity.Order;

public interface OrderService {
    Order createOrder(OrderCreateRequest req);
    Order detail(Long id);
    Order detailByNo(String orderNo);
    PageResult<Order> listMine(Integer status, int page, int pageSize);
    void markPaid(String orderNo, String wxTransactionId);
    void closeExpired();
    void closeOrder(Long id);

    /**
     * 统一下单：校验订单归属与状态后，调用对应支付渠道获取拉起参数。
     */
    UnifiedOrderResult unifiedOrder(Long orderId, String channel);

    /**
     * 发起退款（mock 模式置 REFUNDING；真实模式预留微信退款调用）。
     */
    RefundResult refund(Long orderId, String reason);
}