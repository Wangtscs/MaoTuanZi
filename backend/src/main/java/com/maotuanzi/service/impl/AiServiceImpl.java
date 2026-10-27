package com.maotuanzi.service.impl;

import com.maotuanzi.common.enums.AiTaskStatusEnum;
import com.maotuanzi.common.enums.ErrorCode;
import com.maotuanzi.common.enums.WorkStatusEnum;
import com.maotuanzi.common.exception.BizException;
import com.maotuanzi.dto.AiTaskCreateRequest;
import com.maotuanzi.entity.AiStyle;
import com.maotuanzi.entity.AiTask;
import com.maotuanzi.entity.Pet;
import com.maotuanzi.entity.Work;
import com.maotuanzi.mapper.AiStyleMapper;
import com.maotuanzi.mapper.AiTaskMapper;
import com.maotuanzi.mapper.PetMapper;
import com.maotuanzi.mapper.SystemConfigMapper;
import com.maotuanzi.mapper.WorkMapper;
import com.maotuanzi.security.UserContext;
import com.maotuanzi.service.AiService;
import com.maotuanzi.service.AiProvider;
import com.maotuanzi.service.OssService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * AI 任务服务
 *
 * 真实场景调用阿里云百炼（通义万相）AI进行生成。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

    private final AiTaskMapper aiTaskMapper;
    private final WorkMapper workMapper;
    private final AiStyleMapper aiStyleMapper;
    private final PetMapper petMapper;
    private final SystemConfigMapper configMapper;
    private final StringRedisTemplate redis;
    private final OssService ossService;
    private final AiProvider aiProvider;

    @Value("${maotuanzi.ai.mock-mode:true}")
    private boolean mockMode;

    @Value("${maotuanzi.ai.max-retry:3}")
    private int maxRetry;

    @Value("${maotuanzi.rate-limit.ai-task-concurrent:2}")
    private int maxConcurrent;

    @Override
    @Transactional
    public AiTask createTask(AiTaskCreateRequest req) {
        Long uid = UserContext.getUserId();
        if (uid == null) throw BizException.of(ErrorCode.UNAUTHORIZED);

        Pet pet = petMapper.selectById(req.getPetId());
        if (pet == null || !pet.getUserId().equals(uid)) {
            throw BizException.of(ErrorCode.PET_NOT_FOUND);
        }

        AiStyle style = null;
        if (req.getStyleId() != null) {
            style = aiStyleMapper.selectById(req.getStyleId());
            if (style == null) throw BizException.of(ErrorCode.STYLE_NOT_FOUND);
            if (style.getIsActive() == 0) throw BizException.of(ErrorCode.AI_STYLE_UNAVAILABLE);
        }

        // 并发限制
        Integer running = aiTaskMapper.countRunningByUser(uid);
        if (running != null && running >= maxConcurrent) {
            throw BizException.of(ErrorCode.RATE_LIMIT_AI_TASK);
        }

        AiTask task = new AiTask();
        task.setUserId(uid);
        task.setTaskType(req.getTaskType());
        task.setStyleId(req.getStyleId());
        task.setTemplateId(req.getTemplateId());
        task.setPetId(req.getPetId());
        task.setInputImageUrl(req.getInputImageUrl());
        task.setExtraParams(req.getExtraParams());
        task.setStatus(AiTaskStatusEnum.PENDING.getCode());
        task.setRetryCount(0);
        task.setCostCents(0);
        task.setIsCharged(0);
        task.setProvider("wanx");
        aiTaskMapper.insert(task);

        // 异步处理（生产用 AI 网关）
        processAsync(task.getId());
        return task;
    }

    @Override
    public AiTask queryTask(Long taskId) {
        Long uid = UserContext.getUserId();
        AiTask task = aiTaskMapper.selectById(taskId);
        if (task == null || !task.getUserId().equals(uid)) {
            throw BizException.of(ErrorCode.NOT_FOUND);
        }
        return task;
    }

    @Override
    @Transactional
    public void cancelTask(Long taskId) {
        Long uid = UserContext.getUserId();
        AiTask task = aiTaskMapper.selectById(taskId);
        if (task == null || !task.getUserId().equals(uid)) {
            throw BizException.of(ErrorCode.NOT_FOUND);
        }
        if (task.getStatus() != AiTaskStatusEnum.PENDING.getCode()
                && task.getStatus() != AiTaskStatusEnum.PROCESSING.getCode()) {
            throw BizException.of(ErrorCode.TASK_STATE_INVALID);
        }
        aiTaskMapper.updateStatus(taskId, AiTaskStatusEnum.CANCELLED.getCode(), null, "用户取消", LocalDateTime.now());
    }

    @Override
    @Async
    public void processAsync(Long taskId) {
        AiTask task = aiTaskMapper.selectById(taskId);
        if (task == null) return;
        if (task.getStatus() == AiTaskStatusEnum.CANCELLED.getCode()) return;

        try {
            aiTaskMapper.updateById(setField(task, "status", AiTaskStatusEnum.PROCESSING.getCode(),
                    "startedAt", LocalDateTime.now()));

            // 获取风格信息
            AiStyle style = null;
            if (task.getStyleId() != null) {
                style = aiStyleMapper.selectById(task.getStyleId());
            }

            // 真实AI生成
            String inputUrl = task.getInputImageUrl();
            String styleKey = (style != null) ? style.getCode() : null;
            Map<String, String> aiResult = aiProvider.generate(inputUrl, styleKey, null);
            String outputUrl = aiResult.get("outputUrl");

            if (task.getStatus() == AiTaskStatusEnum.CANCELLED.getCode()) return;

            // 下载AI生成的图片并上传到OSS
            byte[] imageData = downloadImage(outputUrl);
            String ossUrl = ossService.uploadWorkFile(imageData, "work.jpg");

            // 生成作品记录
            Work work = new Work();
            work.setUserId(task.getUserId());
            work.setPetId(task.getPetId());
            work.setTaskId(task.getId());
            work.setWorkType(task.getTaskType());
            work.setTitle(style != null ? style.getName() + " 风格" : "AI 创作");
            work.setPreviewUrl(ossUrl);
            work.setResultUrl(null);
            work.setThumbnailUrl(ossUrl);
            work.setStyleId(task.getStyleId());
            work.setTemplateId(task.getTemplateId());
            work.setStatus(WorkStatusEnum.PREVIEW.getCode());
            work.setIsPublic(0);
            work.setLikeCount(0);
            work.setViewCount(0);
            work.setPreviewExpireAt(LocalDateTime.now().plusHours(24));
            work.setExtra("{\"provider\":\"" + aiProvider.getProviderName() + "\"}");
            workMapper.insert(work);

            aiTaskMapper.updateStatus(taskId, AiTaskStatusEnum.SUCCESS.getCode(), null, null, LocalDateTime.now());
            log.info("AI 任务完成 task_id={} work_id={}", taskId, work.getId());

        } catch (Exception e) {
            log.error("AI 任务失败 task_id={}", taskId, e);
            aiTaskMapper.updateStatus(taskId, AiTaskStatusEnum.FAILED.getCode(),
                    ErrorCode.AI_PROVIDER_ERROR.getCode(), e.getMessage(), LocalDateTime.now());
        }
    }

    private AiTask setField(AiTask t, String name1, Object val1, String name2, Object val2) {
        try {
            var f1 = AiTask.class.getDeclaredField(name1);
            f1.setAccessible(true);
            f1.set(t, val1);
            var f2 = AiTask.class.getDeclaredField(name2);
            f2.setAccessible(true);
            f2.set(t, val2);
        } catch (Exception e) {
            log.warn("字段反射失败", e);
        }
        return t;
    }

    @Override
    public Work getWork(Long workId) {
        Long uid = UserContext.getUserId();
        Work w = workMapper.selectById(workId);
        if (w == null || !w.getUserId().equals(uid)) {
            throw BizException.of(ErrorCode.WORK_NOT_FOUND);
        }
        return w;
    }

    @Override
    public Object queryResult(Long workId) {
        Work w = getWork(workId);
        Map<String, Object> r = new HashMap<>();
        r.put("preview_url", w.getPreviewUrl());
        r.put("preview_expire_at", w.getPreviewExpireAt());
        r.put("is_unlocked", w.getResultUrl() != null);
        r.put("status", w.getStatus());
        return r;
    }

    private byte[] downloadImage(String url) throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);
            try (CloseableHttpResponse response = client.execute(request)) {
                return IOUtils.toByteArray(response.getEntity().getContent());
            }
        }
    }
}