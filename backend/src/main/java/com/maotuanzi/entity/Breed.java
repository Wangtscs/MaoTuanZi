package com.maotuanzi.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class Breed implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private Integer species;
    private String name;
    private String nameEn;
    private String aliases;
    private String description;
    private String characteristics;
    private String careTips;
    private String imageUrl;
    private Integer sortOrder;
    private Integer isActive;
    @JsonIgnore private LocalDateTime createdAt;
    @JsonIgnore private LocalDateTime updatedAt;
}