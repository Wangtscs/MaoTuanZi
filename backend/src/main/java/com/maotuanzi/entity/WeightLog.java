package com.maotuanzi.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class WeightLog implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long userId;
    private Long petId;
    private BigDecimal weightKg;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate recordedAt;
    private String note;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime createdAt;
}