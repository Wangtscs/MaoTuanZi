package com.maotuanzi.service.impl;

import com.maotuanzi.common.enums.ErrorCode;
import com.maotuanzi.common.util.IdUtil;
import com.maotuanzi.common.exception.BizException;
import com.maotuanzi.dto.Model3DTaskResult;
import com.maotuanzi.entity.Model3DTask;
import com.maotuanzi.mapper.Model3DTaskMapper;
import com.maotuanzi.security.UserContext;
import com.maotuanzi.service.Model3DService;
import com.maotuanzi.service.OssService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

/**
 * 3D 模型生成服务实现。
 *
 * <p>数据链路：任务创建（PENDING）→ 占位 glTF 生成 → OSS（可关）→ model_url。
 * 真实 3D 生成模型不在本次范围，但任务 → 产出 → 存储 → URL 的链路已打通。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class Model3DServiceImpl implements Model3DService {

    private static final String MOCK_OSS_BASE = "https://oss.maotuanzi.ai/mock/models/";

    private final Model3DTaskMapper model3DTaskMapper;
    private final OssService ossService;

    @Value("${maotuanzi.oss.enabled:false}")
    private boolean ossEnabled;

    @Override
    @Transactional
    public Model3DTaskResult create(String sourceImageUrl, String style) {
        Long uid = UserContext.getUserId();
        if (uid == null) throw BizException.of(ErrorCode.UNAUTHORIZED);
        if (sourceImageUrl == null || sourceImageUrl.isBlank()) {
            throw BizException.of(ErrorCode.PARAM_MISSING);
        }

        Model3DTask task = new Model3DTask();
        task.setTaskId("3d_" + IdUtil.snowflakeId());
        task.setUserId(uid);
        task.setSourceImageUrl(sourceImageUrl);
        task.setStyle(style);
        task.setStatus("PENDING");
        model3DTaskMapper.insert(task);

        return Model3DTaskResult.builder()
                .taskId(task.getTaskId())
                .status(task.getStatus())
                .modelUrl(null)
                .build();
    }

    @Override
    @Transactional
    public Model3DTaskResult get(String taskId) {
        Long uid = UserContext.getUserId();
        if (uid == null) throw BizException.of(ErrorCode.UNAUTHORIZED);

        Model3DTask task = model3DTaskMapper.selectByTaskId(taskId);
        if (task == null || !task.getUserId().equals(uid)) {
            throw BizException.of(ErrorCode.NOT_FOUND);
        }

        // mock 推进：PENDING → SUCCEEDED，产出占位 glTF
        if ("PENDING".equals(task.getStatus()) || "RUNNING".equals(task.getStatus())) {
            completeTask(task);
            task = model3DTaskMapper.selectByTaskId(taskId);
        }

        return Model3DTaskResult.builder()
                .taskId(task.getTaskId())
                .status(task.getStatus())
                .modelUrl(task.getModelUrl())
                .build();
    }

    private void completeTask(Model3DTask task) {
        try {
            byte[] gltf = buildPlaceholderGltf(task);
            String modelUrl;
            String ossKey;
            if (ossEnabled) {
                modelUrl = ossService.uploadWorkFile(gltf, task.getTaskId() + ".gltf");
                ossKey = modelUrl;
            } else {
                ossKey = "mock/models/" + task.getTaskId() + ".gltf";
                modelUrl = MOCK_OSS_BASE + task.getTaskId() + ".gltf";
            }
            model3DTaskMapper.updateResult(task.getTaskId(), "SUCCEEDED", modelUrl, ossKey, null);
        } catch (Exception e) {
            log.error("3D 任务生成失败 taskId={} err={}", task.getTaskId(), e.getMessage());
            model3DTaskMapper.updateResult(task.getTaskId(), "FAILED", null, null, e.getMessage());
        }
    }

    /** 生成一个最小占位 glTF（JSON 文本），真实 3D 重建模型接入后替换。 */
    private byte[] buildPlaceholderGltf(Model3DTask task) {
        String gltf = "{"
                + "\"asset\":{\"version\":\"2.0\",\"generator\":\"maotuanzi-mock\"},"
                + "\"scene\":0,"
                + "\"scenes\":[{\"nodes\":[0]}],"
                + "\"nodes\":[{\"mesh\":0,\"name\":\"mock_pet\"}],"
                + "\"meshes\":[{\"primitives\":[{\"attributes\":{\"POSITION\":0},\"mode\":4}]}],"
                + "\"buffers\":[{\"byteLength\":0}],"
                + "\"bufferViews\":[{\"buffer\":0,\"byteLength\":0}],"
                + "\"accessors\":[{\"bufferView\":0,\"componentType\":5126,\"count\":0,\"type\":\"VEC3\"}]"
                + "}";
        return gltf.getBytes(StandardCharsets.UTF_8);
    }
}
