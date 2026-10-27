package com.maotuanzi.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 运维-告警记录
 */
@Data
@ApiModel("运维-告警记录")
public class OpsAlarmRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("规则 ID")
    private Long ruleId;

    @ApiModelProperty("规则名称")
    private String ruleName;

    @ApiModelProperty("指标键")
    private String metricKey;

    @ApiModelProperty("触发值")
    private Double metricValue;

    @ApiModelProperty("阈值")
    private Double threshold;

    @ApiModelProperty("等级")
    private String level;

    @ApiModelProperty("状态 FIRING/RESOLVED")
    private String status;

    @ApiModelProperty("告警内容")
    private String message;

    @ApiModelProperty("触发时间")
    private LocalDateTime firedAt;

    @ApiModelProperty("恢复时间")
    private LocalDateTime resolvedAt;

    @ApiModelProperty("持续秒数")
    private Integer durationSeconds;

    @ApiModelProperty("认领人")
    private String ackBy;

    @ApiModelProperty("认领时间")
    private LocalDateTime ackAt;

    @ApiModelProperty("备注")
    private String remark;

    private LocalDateTime createdAt;
}
