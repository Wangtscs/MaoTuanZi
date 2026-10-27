package com.maotuanzi.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@ApiModel("体重记录请求")
public class WeightLogRequest {
    @NotNull
    private Long petId;
    @NotNull
    private BigDecimal weightKg;
    @NotNull
    private LocalDate recordedAt;
    private String note;
}