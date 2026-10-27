package com.maotuanzi.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class SquarePost implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long userId;
    private Long workId;
    private Long petId;
    private String content;
    private String imageUrls;
    private String topic;
    private Integer is3d;
    /** 3D 模型 glTF 地址（is3d=1 时承载 3D 资产） */
    private String modelUrl;
    private Integer likeCount;
    private Integer viewCount;
    private Integer commentCount;
    private Integer shareCount;
    private Integer status;
    private String auditRemark;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime updatedAt;

    @JsonIgnore
    private LocalDateTime deletedAt;
}