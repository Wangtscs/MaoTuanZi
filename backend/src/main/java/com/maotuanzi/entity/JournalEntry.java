package com.maotuanzi.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class JournalEntry implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long userId;
    private Long petId;
    private String title;
    private String content;
    private String images;
    private Integer mood;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate entryDate;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime updatedAt;

    @JsonIgnore
    private LocalDateTime deletedAt;
}