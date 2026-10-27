package com.maotuanzi.controller.ops;

import com.maotuanzi.annotation.RequireAdmin;
import com.maotuanzi.common.result.ApiResult;
import com.maotuanzi.controller.BaseController;
import com.maotuanzi.entity.OpsJob;
import com.maotuanzi.security.AdminContext;
import com.maotuanzi.service.OpsJobService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 任务调度
 */
@Api(tags = "运维平台-任务调度")
@RestController
@RequestMapping("/ops/jobs")
@RequireAdmin(permission = "ops.job")
@RequiredArgsConstructor
public class OpsJobController extends BaseController {

    private final OpsJobService jobService;

    @ApiOperation("任务列表")
    @GetMapping
    public ApiResult<List<OpsJob>> list(@RequestParam(required = false) String keyword,
                                        @RequestParam(required = false) Integer enabled) {
        return ApiResult.ok(jobService.listJobs(keyword, enabled));
    }

    @ApiOperation("任务详情")
    @GetMapping("/{id}")
    public ApiResult<OpsJob> detail(@PathVariable Long id) {
        return ApiResult.ok(jobService.getJob(id));
    }

    @ApiOperation("新增任务")
    @PostMapping
    public ApiResult<Map<String, Object>> create(@RequestBody OpsJob job) {
        Long id = jobService.createJob(job);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id);
        return ApiResult.ok(m);
    }

    @ApiOperation("修改任务")
    @PutMapping("/{id}")
    public ApiResult<Void> update(@PathVariable Long id, @RequestBody OpsJob job) {
        job.setId(id);
        jobService.updateJob(job);
        return ApiResult.ok();
    }

    @ApiOperation("删除任务")
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        jobService.deleteJob(id);
        return ApiResult.ok();
    }

    @ApiOperation("启用 / 停用任务")
    @PutMapping("/{id}/toggle")
    public ApiResult<Void> toggle(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        jobService.toggleJob(id, body.get("enabled"));
        return ApiResult.ok();
    }

    @ApiOperation("立即执行一次")
    @PostMapping("/{id}/trigger")
    public ApiResult<Map<String, Object>> trigger(@PathVariable Long id) {
        Long adminId = AdminContext.getAdminId();
        return ApiResult.ok(jobService.trigger(id, adminId == null ? "unknown" : "admin#" + adminId));
    }

    @ApiOperation("执行记录")
    @GetMapping("/records")
    public ApiResult<Map<String, Object>> records(@RequestParam(required = false) Long jobId,
                                                  @RequestParam(required = false) String status,
                                                  @RequestParam(defaultValue = "1") int page,
                                                  @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResult.ok(jobService.listRecords(jobId, status, page, pageSize));
    }

    @ApiOperation("任务统计")
    @GetMapping("/stat")
    public ApiResult<Map<String, Object>> stat(@RequestParam(defaultValue = "7") int days) {
        return ApiResult.ok(jobService.stat(days));
    }

    @ApiOperation("内置处理器清单")
    @GetMapping("/handlers")
    public ApiResult<List<Map<String, Object>>> handlers() {
        return ApiResult.ok(jobService.handlers());
    }
}
