package com.maotuanzi.controller.api;

import com.maotuanzi.annotation.RequireAuth;
import com.maotuanzi.common.result.ApiResult;
import com.maotuanzi.common.result.PageResult;
import com.maotuanzi.controller.BaseController;
import com.maotuanzi.entity.Notification;
import com.maotuanzi.service.NotificationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Api(tags = "通知")
@RestController
@RequestMapping("/v1/notifications")
@RequireAuth
@RequiredArgsConstructor
public class NotificationController extends BaseController {

    private final NotificationService notificationService;

    @GetMapping
    public ApiResult<PageResult<Notification>> list(@RequestParam(defaultValue = "1") int page,
                                                     @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResult.ok(notificationService.list(page, pageSize));
    }

    @GetMapping("/unread-count")
    public ApiResult<Map<String, Object>> unreadCount() {
        return ApiResult.ok(Map.of("count", notificationService.unreadCount()));
    }

    @PostMapping("/{id}/read")
    public ApiResult<Void> markRead(@PathVariable Long id) {
        notificationService.markRead(id);
        return ApiResult.ok();
    }

    @PostMapping("/read-all")
    public ApiResult<Void> markAllRead() {
        notificationService.markAllRead();
        return ApiResult.ok();
    }
}