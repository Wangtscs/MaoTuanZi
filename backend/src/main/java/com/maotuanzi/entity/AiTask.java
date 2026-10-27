package com.maotuanzi.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class AiTask implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userId;
    private String taskType;
    private Integer styleId;
    private Integer templateId;
    private Long petId;
    private String inputImageUrl;
    private String extraParams;
    private Integer status;
    private Integer retryCount;
    private Integer errorCode;
    private String errorMessage;
    private String provider;
    private String providerTaskId;
    private Integer costCents;
    private Integer isCharged;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime startedAt;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime finishedAt;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime updatedAt;

    @JsonIgnore
    private LocalDateTime deletedAt;
}