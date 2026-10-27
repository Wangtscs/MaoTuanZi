package com.maotuanzi.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@ApiModel("AI 任务创建请求")
public class AiTaskCreateRequest {

    @NotBlank(message = "请选择任务类型")
    @ApiModelProperty(value = "portrait_2d / portrait_3d / emoji / card / feeding / symptom / name")
    private String taskType;

    @ApiModelProperty("风格 ID")
    private Integer styleId;

    @ApiModelProperty("模板 ID")
    private Integer templateId;

    @NotNull(message = "请选择宠物")
    @ApiModelProperty("宠物 ID")
    private Long petId;

    @ApiModelProperty("输入图片 URL（画画像类必填）")
    private String inputImageUrl;

    @ApiModelProperty("额外参数 JSON")
    private String extraParams;
}