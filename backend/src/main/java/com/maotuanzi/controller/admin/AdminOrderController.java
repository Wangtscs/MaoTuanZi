package com.maotuanzi.controller.admin;

import com.maotuanzi.annotation.RequireAdmin;
import com.maotuanzi.common.result.ApiResult;
import com.maotuanzi.common.result.PageResult;
import com.maotuanzi.controller.BaseController;
import com.maotuanzi.entity.Order;
import com.maotuanzi.entity.Refund;
import com.maotuanzi.service.AdminService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Api(tags = "管理后台-订单")
@RestController
@RequestMapping("/admin/orders")
@RequireAdmin
@RequiredArgsConstructor
public class AdminOrderController extends BaseController {

    private final AdminService adminService;

    @GetMapping
    public ApiResult<PageResult<Order>> list(@RequestParam(required = false) String keyword,
                                              @RequestParam(required = false) Integer status,
                                              @RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResult.ok(adminService.listOrders(keyword, status, page, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResult<Order> detail(@PathVariable Long id) {
        return ApiResult.ok(adminService.getOrder(id));
    }

    @GetMapping("/refunds")
    public ApiResult<PageResult<Refund>> refunds(@RequestParam(required = false) Integer status,
                                                 @RequestParam(defaultValue = "1") int page,
                                                 @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResult.ok(adminService.listRefunds(status, page, pageSize));
    }

    @PostMapping("/refunds/{id}/review")
    @RequireAdmin(permission = "refund.review")
    public ApiResult<Void> reviewRefund(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        boolean approve = Boolean.TRUE.equals(body.get("approve"));
        adminService.reviewRefund(id, approve, (String) body.get("remark"));
        return ApiResult.ok();
    }
}