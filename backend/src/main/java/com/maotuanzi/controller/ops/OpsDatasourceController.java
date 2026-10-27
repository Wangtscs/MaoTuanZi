package com.maotuanzi.controller.ops;

import com.maotuanzi.annotation.RequireAdmin;
import com.maotuanzi.common.result.ApiResult;
import com.maotuanzi.controller.BaseController;
import com.maotuanzi.service.OpsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 数据源（MySQL / Redis）
 */
@Api(tags = "运维平台-数据源")
@RestController
@RequestMapping("/ops/datasource")
@RequireAdmin(permission = "ops.datasource")
@RequiredArgsConstructor
public class OpsDatasourceController extends BaseController {

    private final OpsService opsService;

    @ApiOperation("数据库状态与连接池")
    @GetMapping("/db")
    public ApiResult<Map<String, Object>> db() {
        return ApiResult.ok(opsService.dbStatus());
    }

    @ApiOperation("库表容量统计")
    @GetMapping("/db/tables")
    public ApiResult<List<Map<String, Object>>> tables() {
        return ApiResult.ok(opsService.dbTables());
    }

    @ApiOperation("Redis 状态与键分布")
    @GetMapping("/redis")
    public ApiResult<Map<String, Object>> redis() {
        return ApiResult.ok(opsService.redisStatus());
    }
}
