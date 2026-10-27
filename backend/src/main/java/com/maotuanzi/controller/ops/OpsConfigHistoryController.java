package com.maotuanzi.controller.ops;

import com.maotuanzi.annotation.RequireAdmin;
import com.maotuanzi.common.result.ApiResult;
import com.maotuanzi.ops.OpsConfigHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/ops/configs")
@RequireAdmin(permission = "ops.config")
@RequiredArgsConstructor
public class OpsConfigHistoryController {
    private final OpsConfigHistory history;
    @GetMapping("/history")
    public ApiResult<List<Map<String, Object>>> history(@RequestParam(required = false) String key) {
        return ApiResult.ok(history.history(key));
    }
    @PostMapping("/rollback")
    public ApiResult<Void> rollback(@RequestBody Map<String, String> body) {
        history.rollback(body.get("key"), body.get("revision"));
        return ApiResult.ok();
    }
}
