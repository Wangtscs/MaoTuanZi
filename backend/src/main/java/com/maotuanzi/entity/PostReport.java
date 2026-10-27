package com.maotuanzi.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class PostReport implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long postId;
    private Long userId;
    private Integer type;
    private String reason;
    private Integer status;
    private String handledBy;
    private String handledRemark;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime handledAt;
}