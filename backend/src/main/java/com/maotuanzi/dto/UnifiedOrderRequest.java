package com.maotuanzi.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@ApiModel("统一下单请求")
public class UnifiedOrderRequest {

    @NotNull(message = "请指定订单")
    @ApiModelProperty("订单 ID")
    private Long orderId;

    @NotBlank(message = "请指定支付渠道")
    @ApiModelProperty("支付渠道 WECHAT / HUAWEI")
    private String channel;
}
