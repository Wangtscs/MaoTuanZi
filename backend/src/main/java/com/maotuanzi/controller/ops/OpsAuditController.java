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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 审计日志（管理员操作 + 内容安全审核）
 */
@Api(tags = "运维平台-审计")
@RestController
@RequestMapping("/ops/audit")
@RequireAdmin(permission = "ops.audit")
@RequiredArgsConstructor
public class OpsAuditController extends BaseController {

    private final OpsService opsService;

    @ApiOperation("管理员操作日志")
    @GetMapping("/admin-logs")
    public ApiResult<Map<String, Object>> adminLogs(@RequestParam(required = false) String module,
                                                    @RequestParam(required = false) String action,
                                                    @RequestParam(required = false) String keyword,
                                                    @RequestParam(required = false) String from,
                                                    @RequestParam(required = false) String to,
                                                    @RequestParam(defaultValue = "1") int page,
                                                    @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResult.ok(opsService.adminLogs(module, action, keyword, from, to, page, pageSize));
    }

    @ApiOperation("内容安全审核日志")
    @GetMapping("/content-logs")
    public ApiResult<Map<String, Object>> contentLogs(@RequestParam(required = false) String checkResult,
                                                      @RequestParam(required = false) String contentType,
                                                      @RequestParam(required = false) String keyword,
                                                      @RequestParam(required = false) String from,
                                                      @RequestParam(required = false) String to,
                                                      @RequestParam(defaultValue = "1") int page,
                                                      @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResult.ok(opsService.contentAuditLogs(checkResult, contentType, keyword, from, to, page, pageSize));
    }

    @ApiOperation("审计统计")
    @GetMapping("/stat")
    public ApiResult<Map<String, Object>> stat(@RequestParam(defaultValue = "7") int days) {
        return ApiResult.ok(opsService.auditStat(days));
    }
}
