package com.maotuanzi.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class PostComment implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long postId;
    private Long userId;
    private Long parentId;
    private String content;
    private Integer status;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime createdAt;

    @JsonIgnore
    private LocalDateTime deletedAt;
}