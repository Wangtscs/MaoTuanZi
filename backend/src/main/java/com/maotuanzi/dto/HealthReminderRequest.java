package com.maotuanzi.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@ApiModel("健康提醒请求")
public class HealthReminderRequest {

    @NotNull(message = "请选择宠物")
    private Long petId;

    @NotNull(message = "请选择类型")
    private Integer type;

    @NotBlank(message = "请填写标题")
    private String title;

    private Integer cycleDays;

    @NotNull(message = "请选择首次提醒时间")
    private LocalDateTime nextRunAt;

    private String note;
}