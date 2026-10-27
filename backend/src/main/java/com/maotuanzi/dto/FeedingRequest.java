package com.maotuanzi.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@ApiModel("喂养记录请求")
public class FeedingRequest {
    @NotNull
    private Long petId;
    private LocalDateTime fedAt;
    private String foodType;
    private Integer amountG;
    private String note;
}