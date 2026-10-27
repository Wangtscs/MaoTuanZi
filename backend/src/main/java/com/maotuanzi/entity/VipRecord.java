package com.maotuanzi.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class VipRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userId;
    private Long orderId;
    private Integer changeType;
    private Integer vipLevelBefore;
    private Integer vipLevelAfter;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime expireBefore;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime expireAfter;
    private Integer durationDays;
    private Integer amountCents;
    private String remark;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime createdAt;
}