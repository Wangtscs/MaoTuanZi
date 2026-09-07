package com.maotuanzi.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel("分享记录请求")
public class ShareRecordRequest {

    @ApiModelProperty("关联帖子 ID（可为空，仅统计分享数）")
    private Long postId;

    @NotBlank(message = "请指定分享渠道")
    @ApiModelProperty("分享渠道，如 wechat / moments / huawei")
    private String channel;
}
