package com.maotuanzi.controller.api;

import com.maotuanzi.annotation.Idempotent;
import com.maotuanzi.annotation.RequireAuth;
import com.maotuanzi.common.result.ApiResult;
import com.maotuanzi.common.result.PageResult;
import com.maotuanzi.controller.BaseController;
import com.maotuanzi.dto.OrderCreateRequest;
import com.maotuanzi.dto.RefundRequest;
import com.maotuanzi.dto.RefundResult;
import com.maotuanzi.dto.UnifiedOrderRequest;
import com.maotuanzi.dto.UnifiedOrderResult;
import com.maotuanzi.entity.Order;
import com.maotuanzi.service.OrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@Api(tags = "订单")
@RestController
@RequestMapping("/v1/orders")
@RequireAuth
@RequiredArgsConstructor
public class OrderController extends BaseController {

    private final OrderService orderService;

    @ApiOperation("创建订单")
    @PostMapping
    @Idempotent
    public ApiResult<Order> create(@Valid @RequestBody OrderCreateRequest req, HttpServletRequest http) {
        if (req.getClientIp() == null) req.setClientIp(getClientIp(http));
        return ApiResult.ok(orderService.createOrder(req));
    }

    @ApiOperation("详情")
    @GetMapping("/{id}")
    public ApiResult<Order> detail(@PathVariable Long id) {
        return ApiResult.ok(orderService.detail(id));
    }

    @ApiOperation("我的订单列表")
    @GetMapping
    public ApiResult<PageResult<Order>> list(@RequestParam(required = false) Integer status,
                                              @RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResult.ok(orderService.listMine(status, page, pageSize));
    }

    @ApiOperation("关闭订单")
    @PostMapping("/{id}/close")
    public ApiResult<Void> close(@PathVariable Long id) {
        orderService.closeOrder(id);
        return ApiResult.ok();
    }

    @ApiOperation("统一下单（双支付：微信 / 华为）")
    @PostMapping("/unified-order")
    public ApiResult<UnifiedOrderResult> unifiedOrder(@Valid @RequestBody UnifiedOrderRequest req) {
        return ApiResult.ok(orderService.unifiedOrder(req.getOrderId(), req.getChannel()));
    }

    @ApiOperation("申请退款（返回 refundId / status）")
    @PostMapping("/{id}/refund")
    public ApiResult<RefundResult> refund(@PathVariable Long id,
                                          @RequestBody(required = false) RefundRequest req) {
        String reason = req == null ? null : req.getReason();
        return ApiResult.ok(orderService.refund(id, reason));
    }

    private String getClientIp(HttpServletRequest req) {
        String ip = req.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty()) return ip.split(",")[0].trim();
        return req.getRemoteAddr();
    }
}