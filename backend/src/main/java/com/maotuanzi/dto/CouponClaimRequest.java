package com.maotuanzi.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 卡券领取请求（对齐前端契约：couponId 为字符串，可为卡券 code 或数字 id）。
 */
@Data
@ApiModel("卡券领取请求")
public class CouponClaimRequest {

    @NotBlank(message = "请指定卡券")
    @ApiModelProperty("卡券 code 或数字 id")
    private String couponId;
}
