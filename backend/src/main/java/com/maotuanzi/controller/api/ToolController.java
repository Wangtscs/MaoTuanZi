package com.maotuanzi.controller.api;

import com.maotuanzi.annotation.RequireAuth;
import com.maotuanzi.common.result.ApiResult;
import com.maotuanzi.controller.BaseController;
import com.maotuanzi.dto.FeedingRequest;
import com.maotuanzi.dto.HealthReminderRequest;
import com.maotuanzi.dto.JournalRequest;
import com.maotuanzi.dto.WeightLogRequest;
import com.maotuanzi.entity.*;
import com.maotuanzi.service.ToolService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(tags = "养宠工具")
@RestController
@RequestMapping("/v1/tools")
@RequireAuth
@RequiredArgsConstructor
public class ToolController extends BaseController {

    private final ToolService toolService;

    // 健康提醒
    @ApiOperation("健康提醒列表")
    @GetMapping("/health-reminders")
    public ApiResult<List<HealthReminder>> reminders(@RequestParam(required = false) Long petId) {
        return ApiResult.ok(toolService.listReminders(petId));
    }

    @PostMapping("/health-reminders")
    public ApiResult<HealthReminder> createReminder(@Valid @RequestBody HealthReminderRequest req) {
        return ApiResult.ok(toolService.createReminder(req));
    }

    @PatchMapping("/health-reminders/{id}")
    public ApiResult<HealthReminder> updateReminder(@PathVariable Long id, @Valid @RequestBody HealthReminderRequest req) {
        return ApiResult.ok(toolService.updateReminder(id, req));
    }

    @DeleteMapping("/health-reminders/{id}")
    public ApiResult<Void> deleteReminder(@PathVariable Long id) {
        toolService.deleteReminder(id);
        return ApiResult.ok();
    }

    // 体重
    @GetMapping("/weights")
    public ApiResult<List<WeightLog>> weights(@RequestParam Long petId) {
        return ApiResult.ok(toolService.listWeights(petId));
    }

    @PostMapping("/weights")
    public ApiResult<WeightLog> addWeight(@Valid @RequestBody WeightLogRequest req) {
        return ApiResult.ok(toolService.addWeight(req));
    }

    @DeleteMapping("/weights/{id}")
    public ApiResult<Void> deleteWeight(@PathVariable Long id) {
        toolService.deleteWeight(id);
        return ApiResult.ok();
    }

    // 时光
    @GetMapping("/journal")
    public ApiResult<List<JournalEntry>> journal(@RequestParam Long petId,
                                                  @RequestParam(defaultValue = "1") int page,
                                                  @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResult.ok(toolService.listJournal(petId, page, pageSize));
    }

    @PostMapping("/journal")
    public ApiResult<JournalEntry> addJournal(@Valid @RequestBody JournalRequest req) {
        return ApiResult.ok(toolService.addJournal(req));
    }

    @PatchMapping("/journal/{id}")
    public ApiResult<JournalEntry> updateJournal(@PathVariable Long id, @Valid @RequestBody JournalRequest req) {
        return ApiResult.ok(toolService.updateJournal(id, req));
    }

    @DeleteMapping("/journal/{id}")
    public ApiResult<Void> deleteJournal(@PathVariable Long id) {
        toolService.deleteJournal(id);
        return ApiResult.ok();
    }

    // 喂养
    @GetMapping("/feedings")
    public ApiResult<List<Feeding>> feedings(@RequestParam Long petId,
                                              @RequestParam(defaultValue = "20") int limit) {
        return ApiResult.ok(toolService.listFeedings(petId, limit));
    }

    @PostMapping("/feedings")
    public ApiResult<Feeding> addFeeding(@Valid @RequestBody FeedingRequest req) {
        return ApiResult.ok(toolService.addFeeding(req));
    }

    @DeleteMapping("/feedings/{id}")
    public ApiResult<Void> deleteFeeding(@PathVariable Long id) {
        toolService.deleteFeeding(id);
        return ApiResult.ok();
    }

    // 喂养方案
    @PostMapping("/feeding-plan")
    public ApiResult<Object> generatePlan(@RequestBody Map<String, Long> body) {
        return ApiResult.ok(toolService.generateFeedingPlan(body.get("petId")));
    }

    @GetMapping("/feeding-plan")
    public ApiResult<Object> queryPlan(@RequestParam Long petId) {
        Object plan = toolService.queryFeedingPlan(petId);
        if (plan == null) {
            Map<String, Object> r = new HashMap<>();
            r.put("exists", false);
            return ApiResult.ok(r);
        }
        Map<String, Object> r = new HashMap<>(plan instanceof Map ? (Map<String, Object>) plan : new HashMap<>());
        r.put("exists", true);
        return ApiResult.ok(r);
    }

    // 症状查询（基础 mock）
    @GetMapping("/symptom")
    public ApiResult<Object> symptom(@RequestParam String keyword) {
        Map<String, Object> r = new HashMap<>();
        r.put("summary", "关于「" + keyword + "」的护理参考");
        r.put("common_causes", List.of("近期饮食变化", "环境应激", "轻微菌群失调"));
        r.put("home_care", List.of("暂停零食 24 小时", "少食多餐", "保证饮水清洁"));
        r.put("warning_signs", List.of("便血", "持续呕吐", "精神萎靡超过 24 小时", "拒食超过 24 小时"));
        r.put("disclaimer", "⚠️ 本内容仅供参考，不构成兽医诊断、医疗建议或治疗方案。若宠物出现持续不适、急性症状或疑似中毒，请立即前往正规宠物医院就诊。");
        return ApiResult.ok(r);
    }
}