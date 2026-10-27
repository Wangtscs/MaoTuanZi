package com.maotuanzi.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class Checkin implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long userId;
    private Long petId;
    private String topic;
    private String content;
    private String imageUrls;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkinDate;
    private Integer consecutiveDays;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime createdAt;
}