package com.maotuanzi.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class AiStyle implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private String category;
    private String name;
    private String code;
    private String description;
    private String previewUrl;
    private String promptTemplate;
    private Integer isVipOnly;
    private Integer priceCents;
    private Integer isActive;
    private Integer sortOrder;
    @JsonIgnore private LocalDateTime createdAt;
    @JsonIgnore private LocalDateTime updatedAt;
}