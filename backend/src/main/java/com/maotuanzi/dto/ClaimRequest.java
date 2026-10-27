package com.maotuanzi.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 领取奖励请求（对齐前端契约：rewardId 为字符串）。
 */
@Data
@ApiModel("领取奖励请求")
public class ClaimRequest {

    @NotBlank(message = "请指定奖励 ID")
    @ApiModelProperty("奖励 ID")
    private String rewardId;
}
