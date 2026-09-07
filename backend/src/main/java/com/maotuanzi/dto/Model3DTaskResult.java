package com.maotuanzi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 3D 模型任务结果（对齐前端契约：taskId / status / modelUrl）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Model3DTaskResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 任务 ID */
    private String taskId;

    /** 任务状态 PENDING / RUNNING / SUCCEEDED / FAILED */
    private String status;

    /** 生成的 glTF 模型地址，未产出时为 null */
    private String modelUrl;
}
