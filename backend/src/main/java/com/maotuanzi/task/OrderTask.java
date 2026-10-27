package com.maotuanzi.task;

import com.maotuanzi.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTask {

    private final OrderService orderService;

    /** 每分钟关闭过期订单 */
    @Scheduled(fixedDelay = 60_000L)
    public void closeExpiredOrders() {
        try {
            orderService.closeExpired();
        } catch (Exception e) {
            log.error("关闭过期订单失败", e);
        }
    }
}