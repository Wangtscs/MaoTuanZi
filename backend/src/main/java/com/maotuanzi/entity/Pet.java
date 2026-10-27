package com.maotuanzi.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class Pet implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userId;
    private String nickname;
    private Integer species;
    private Integer breedId;
    private String breedName;
    private Integer gender;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;
    private BigDecimal weightKg;
    private Integer isNeutered;
    private String allergies;       // JSON 字符串
    private String stapleFood;
    private String avatarUrl;
    private String remark;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime updatedAt;

    @JsonIgnore
    private LocalDateTime deletedAt;
}