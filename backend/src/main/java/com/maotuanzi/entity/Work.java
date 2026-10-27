package com.maotuanzi.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class Work implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userId;
    private Long petId;
    private Long taskId;
    private String workType;
    private String title;
    private String previewUrl;
    private String resultUrl;
    private String thumbnailUrl;
    private Integer styleId;
    private Integer templateId;
    private Integer status;
    private Integer isPublic;
    private Integer likeCount;
    private Integer viewCount;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime previewExpireAt;
    private String extra;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime updatedAt;

    @JsonIgnore
    private LocalDateTime deletedAt;
}