package com.maotuanzi.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 绑定邀请码请求（对齐前端契约：inviterCode / inviteeUserId）。
 */
@Data
@ApiModel("绑定邀请码请求")
public class InviteRequest {

    @NotBlank(message = "请填写邀请码")
    @ApiModelProperty("邀请人的邀请码")
    private String inviterCode;

    @ApiModelProperty("被邀请人用户 ID（服务端以登录态为准，此字段用于前端契约对齐）")
    private String inviteeUserId;
}
