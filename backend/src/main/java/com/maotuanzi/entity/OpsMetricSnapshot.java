package com.maotuanzi.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 运维-指标快照
 */
@Data
@ApiModel("运维-指标快照")
public class OpsMetricSnapshot implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("实例 ID")
    private String instanceId;

    @ApiModelProperty("指标键")
    private String metricKey;

    @ApiModelProperty("指标值")
    private Double metricValue;

    @ApiModelProperty("采样时间")
    private LocalDateTime sampleTime;

    private LocalDateTime createdAt;
}
