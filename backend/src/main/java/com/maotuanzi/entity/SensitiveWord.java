package com.maotuanzi.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class SensitiveWord implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer id;
    private String word;
    private String category;
    private Integer severity;
    private Integer isActive;
    @JsonIgnore private LocalDateTime createdAt;
}