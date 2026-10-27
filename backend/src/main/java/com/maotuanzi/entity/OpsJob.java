package com.maotuanzi.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 运维-定时任务
 */
@Data
@ApiModel("运维-定时任务")
public class OpsJob implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("任务编码")
    private String jobCode;

    @ApiModelProperty("任务名称")
    private String jobName;

    @ApiModelProperty("Cron 表达式（6 位）")
    private String cron;

    @ApiModelProperty("处理器标识")
    private String handler;

    @ApiModelProperty("任务类型 builtin/manual")
    private String jobType;

    @ApiModelProperty("参数 JSON")
    private String params;

    @ApiModelProperty("是否启用")
    private Integer enabled;

    @ApiModelProperty("最近执行状态")
    private String lastStatus;

    @ApiModelProperty("最近执行时间")
    private LocalDateTime lastRunAt;

    @ApiModelProperty("下次执行时间")
    private LocalDateTime nextRunAt;

    @ApiModelProperty("最近耗时 ms")
    private Long lastDurationMs;

    @ApiModelProperty("最近执行信息")
    private String lastMessage;

    @ApiModelProperty("执行次数")
    private Integer runCount;

    @ApiModelProperty("失败次数")
    private Integer failCount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
