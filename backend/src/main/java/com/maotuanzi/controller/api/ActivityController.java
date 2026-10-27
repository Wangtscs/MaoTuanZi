package com.maotuanzi.controller.api;

import com.maotuanzi.annotation.RequireAuth;
import com.maotuanzi.common.result.ApiResult;
import com.maotuanzi.controller.BaseController;
import com.maotuanzi.entity.SystemConfig;
import com.maotuanzi.mapper.SystemConfigMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@Api(tags = "活动")
@RestController
@RequestMapping("/v1/activities")
@RequiredArgsConstructor
public class ActivityController extends BaseController {

    private final SystemConfigMapper configMapper;

    @ApiOperation("限时活动倒计时")
    @GetMapping("/countdown")
    @RequireAuth
    public ApiResult<Map<String, Object>> countdown() {
        Map<String, Object> r = new HashMap<>();

        // 从数据库读取活动配置（key = "activity_countdown_end_time"，存毫秒时间戳）
        SystemConfig cfg = configMapper.selectByKey("activity_countdown_end_time");
        long endTime;
        if (cfg != null && cfg.getConfigValue() != null) {
            try {
                endTime = Long.parseLong(cfg.getConfigValue());
            } catch (NumberFormatException e) {
                endTime = System.currentTimeMillis() + 7 * 24 * 3600 * 1000L;
            }
        } else {
            // 无配置时默认 +7 天
            endTime = System.currentTimeMillis() + 7 * 24 * 3600 * 1000L;
        }

        r.put("endTime", endTime);
        r.put("title", "限时优惠");
        r.put("description", "新用户首单立减50%");
        r.put("active", endTime > System.currentTimeMillis());
        return ApiResult.ok(r);
    }
}
