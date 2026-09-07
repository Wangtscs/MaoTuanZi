package com.maotuanzi.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 3D 模型生成任务。
 *
 * <p>数据链路：任务创建（PENDING）→ 内部 AI 端点 / 占位 glTF 生成 → 上传 OSS → model_url 产出。</p>
 */
@Data
public class Model3DTask implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    /** 业务任务号（对外暴露，唯一） */
    private String taskId;
    private Long userId;
    /** 输入的宠物原图 URL */
    private String sourceImageUrl;
    /** 3D 风格 / 参数 */
    private String style;
    /** 状态：PENDING / RUNNING / SUCCEEDED / FAILED */
    private String status;
    /** 产出的 glTF 模型地址 */
    private String modelUrl;
    /** OSS object key（OSS 关闭时为占位 key） */
    private String ossKey;
    /** 失败原因 */
    private String errorMsg;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime updatedAt;
}
