package com.maotuanzi.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 运维-任务执行记录
 */
@Data
@ApiModel("运维-任务执行记录")
public class OpsJobRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("任务 ID")
    private Long jobId;

    @ApiModelProperty("任务编码")
    private String jobCode;

    @ApiModelProperty("状态 RUNNING/SUCCESS/FAILED")
    private String status;

    @ApiModelProperty("触发方式 AUTO/MANUAL")
    private String triggerType;

    @ApiModelProperty("执行实例")
    private String instanceId;

    @ApiModelProperty("开始时间")
    private LocalDateTime startAt;

    @ApiModelProperty("结束时间")
    private LocalDateTime endAt;

    @ApiModelProperty("耗时 ms")
    private Long durationMs;

    @ApiModelProperty("执行信息")
    private String message;

    private LocalDateTime createdAt;
}
