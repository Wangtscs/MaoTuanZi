package com.maotuanzi.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class Product implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private String description;
    private Integer productType;
    private Integer priceCents;
    private Integer originalPriceCents;
    private Integer durationDays;
    private Integer quotaCount;
    private String quotaPeriod;
    private Integer vipLevelRequired;
    private String imageUrl;
    private Integer isActive;
    private Integer sortOrder;
    @JsonIgnore private LocalDateTime createdAt;
    @JsonIgnore private LocalDateTime updatedAt;
}