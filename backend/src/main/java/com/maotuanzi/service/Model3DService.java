package com.maotuanzi.service;

import com.maotuanzi.dto.Model3DTaskResult;

/**
 * 3D 模型生成服务。
 */
public interface Model3DService {

    /**
     * 创建 3D 生成任务（初始 PENDING）。
     */
    Model3DTaskResult create(String sourceImageUrl, String style);

    /**
     * 查询任务；PENDING 任务在 mock 模式下同步推进为 SUCCEEDED 并产出 glTF。
     */
    Model3DTaskResult get(String taskId);
}
