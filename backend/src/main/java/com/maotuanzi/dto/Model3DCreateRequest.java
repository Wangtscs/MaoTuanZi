package com.maotuanzi.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 3D 模型生成请求（对齐前端契约：sourceImageUrl / style）。
 */
@Data
@ApiModel("3D 模型生成请求")
public class Model3DCreateRequest {

    @NotBlank(message = "请提供原图 URL")
    @ApiModelProperty("已上传到 OSS 的宠物原图 URL")
    private String sourceImageUrl;

    @ApiModelProperty("3D 风格/参数，可为空")
    private String style;
}
