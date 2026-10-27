package com.maotuanzi.controller.ops;

import com.maotuanzi.annotation.RequireAdmin;
import com.maotuanzi.common.result.ApiResult;
import com.maotuanzi.ops.OpsNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/ops/alarm/deliveries")
@RequireAdmin(permission = "ops.alarm")
@RequiredArgsConstructor
public class OpsNotificationController {
    private final OpsNotificationService notifications;
    @GetMapping
    public ApiResult<List<Map<String, Object>>> receipts() { return ApiResult.ok(notifications.receipts()); }
    @PostMapping("/{id}/retry")
    public ApiResult<Map<String, Object>> retry(@PathVariable String id) { return ApiResult.ok(notifications.retry(id)); }
}
