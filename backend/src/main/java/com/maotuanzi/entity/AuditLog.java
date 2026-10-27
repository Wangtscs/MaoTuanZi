package com.maotuanzi.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class AuditLog implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long userId;
    private String contentType;
    private String contentId;
    private String checkType;
    private String checkResult;
    private String reason;
    private String operator;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime createdAt;
}