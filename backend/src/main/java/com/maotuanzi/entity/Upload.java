package com.maotuanzi.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class Upload implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long userId;
    private String ossKey;
    private String url;
    private String fileName;
    private String contentType;
    private Long sizeBytes;
    private String bizType;
    private String relatedId;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime createdAt;
}