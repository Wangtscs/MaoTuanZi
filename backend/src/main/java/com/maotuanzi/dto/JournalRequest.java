package com.maotuanzi.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@ApiModel("时光手册请求")
public class JournalRequest {
    @NotNull
    private Long petId;
    @NotBlank
    private String title;
    private String content;
    private String images;
    private Integer mood;
    private LocalDate entryDate;
}