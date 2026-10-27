package com.maotuanzi.controller;

import com.maotuanzi.common.result.ApiResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/v1")
public class HealthController extends BaseController {

    @GetMapping("/health")
    public ApiResult<Map<String, Object>> health() {
        Map<String, Object> r = new HashMap<>();
        r.put("status", "UP");
        r.put("timestamp", System.currentTimeMillis());
        r.put("service", "maotuanzi-api");
        r.put("version", "2.0.0");
        return ApiResult.ok(r);
    }
}