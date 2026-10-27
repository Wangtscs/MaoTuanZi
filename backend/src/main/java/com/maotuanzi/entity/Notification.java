package com.maotuanzi.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class Notification implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long userId;
    private String type;
    private String title;
    private String content;
    private String payload;
    private Integer isRead;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime readAt;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime createdAt;
}