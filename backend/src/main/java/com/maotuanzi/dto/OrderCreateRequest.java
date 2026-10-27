package com.maotuanzi.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel("订单创建请求")
public class OrderCreateRequest {

    @NotBlank(message = "请选择商品")
    @ApiModelProperty("商品 ID")
    private String productId;

    @ApiModelProperty("关联作品 ID（单次付费类必填）")
    private Long workId;

    @ApiModelProperty("宠物 ID（喂养方案类必填）")
    private Long petId;

    @ApiModelProperty("卡券 ID")
    private Long couponId;

    @ApiModelProperty("客户端 IP（用于回调）")
    private String clientIp;
}