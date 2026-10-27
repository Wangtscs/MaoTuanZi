package com.maotuanzi.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * 分页请求基类
 */
@Data
@ApiModel("分页请求")
public class PageRequest {

    @ApiModelProperty(value = "页码，从 1 开始", example = "1")
    @Min(value = 1, message = "页码不能小于 1")
    private Integer page = 1;

    @ApiModelProperty(value = "每页大小", example = "20")
    @Min(value = 1, message = "每页大小不能小于 1")
    @Max(value = 100, message = "每页大小不能大于 100")
    private Integer pageSize = 20;
}