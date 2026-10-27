package com.maotuanzi.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class HealthReminder implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long userId;
    private Long petId;
    private Integer type;
    private String title;
    private Integer cycleDays;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime nextRunAt;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime lastRunAt;
    private String note;
    private Integer isActive;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime updatedAt;

    @JsonIgnore
    private LocalDateTime deletedAt;
}