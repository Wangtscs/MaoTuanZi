package com.maotuanzi.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Coupon implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String code;
    private String name;
    private Integer type;
    private Integer discountCents;
    private BigDecimal discountRate;
    private Integer minAmountCents;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime validFrom;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime validTo;
    private Integer totalCount;
    private Integer remainCount;
    private Integer perUserLimit;
    private String scope;
    private Integer isActive;
    @JsonIgnore private LocalDateTime createdAt;
    @JsonIgnore private LocalDateTime updatedAt;
}