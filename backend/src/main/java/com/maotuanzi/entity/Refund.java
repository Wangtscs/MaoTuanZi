package com.maotuanzi.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class Refund implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String refundNo;
    private Long orderId;
    private Long userId;
    private Integer amountCents;
    private String reason;
    private Integer status;
    private String wxRefundId;
    private String reviewRemark;
    private String reviewedBy;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime reviewedAt;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime refundedAt;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime updatedAt;
}