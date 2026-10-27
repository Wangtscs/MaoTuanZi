package com.maotuanzi.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class SystemConfig implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer id;
    private String configKey;
    private String configValue;
    private String description;
    private Integer isPublic;
    private String updatedBy;
    @JsonIgnore private LocalDateTime createdAt;
    @JsonIgnore private LocalDateTime updatedAt;
}