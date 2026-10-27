package com.maotuanzi.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel("微信登录请求")
public class WxLoginRequest {
    @NotBlank(message = "code 不能为空")
    @ApiModelProperty(value = "微信 jscode")
    private String code;

    @ApiModelProperty("昵称")
    private String nickname;

    @ApiModelProperty("头像 URL")
    private String avatarUrl;

    @ApiModelProperty("协议版本")
    private String agreementVersion;
}