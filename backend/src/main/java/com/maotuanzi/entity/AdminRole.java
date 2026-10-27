package com.maotuanzi.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class AdminRole implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer id;
    private String code;
    private String name;
    private String description;
    private Integer isSystem;
    @JsonIgnore private LocalDateTime createdAt;
    @JsonIgnore private LocalDateTime updatedAt;
}