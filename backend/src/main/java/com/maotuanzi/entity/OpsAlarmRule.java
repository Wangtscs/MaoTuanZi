package com.maotuanzi.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 运维-告警规则
 */
@Data
@ApiModel("运维-告警规则")
public class OpsAlarmRule implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("规则名称")
    private String name;

    @ApiModelProperty("监控指标键")
    private String metricKey;

    @ApiModelProperty("比较符 gt/gte/lt/lte/eq")
    private String operator;

    @ApiModelProperty("阈值")
    private Double threshold;

    @ApiModelProperty("连续满足次数")
    private Integer durationChecks;

    @ApiModelProperty("告警等级 P0/P1/P2")
    private String level;

    @ApiModelProperty("是否启用")
    private Integer enabled;

    @ApiModelProperty("静默分钟")
    private Integer silenceMinutes;

    @ApiModelProperty("通知渠道")
    private String channels;

    @ApiModelProperty("描述")
    private String description;

    @ApiModelProperty("最近一次取值")
    private Double lastValue;

    @ApiModelProperty("最近检查时间")
    private LocalDateTime lastCheckAt;

    @ApiModelProperty("是否处于告警中")
    private Integer firing;

    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
