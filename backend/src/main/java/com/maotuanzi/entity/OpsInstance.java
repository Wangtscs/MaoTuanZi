package com.maotuanzi.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 运维-服务实例
 */
@Data
@ApiModel("运维-服务实例")
public class OpsInstance implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("实例唯一标识")
    private String instanceId;

    @ApiModelProperty("应用名")
    private String appName;

    @ApiModelProperty("运行环境")
    private String profile;

    @ApiModelProperty("主机名")
    private String host;

    @ApiModelProperty("IP")
    private String ip;

    @ApiModelProperty("端口")
    private Integer port;

    @ApiModelProperty("进程号")
    private String pid;

    @ApiModelProperty("版本号")
    private String version;

    @ApiModelProperty("状态 UP/DEGRADED/DOWN")
    private String status;

    @ApiModelProperty("进程 CPU 负载")
    private Double cpuLoad;

    @ApiModelProperty("系统 CPU 负载")
    private Double systemCpuLoad;

    @ApiModelProperty("已用堆内存 MB")
    private Long heapUsedMb;

    @ApiModelProperty("最大堆内存 MB")
    private Long heapMaxMb;

    @ApiModelProperty("堆内存使用率")
    private Double heapUsedPct;

    @ApiModelProperty("线程数")
    private Integer threadCount;

    @ApiModelProperty("QPS")
    private Double qps;

    @ApiModelProperty("错误率")
    private Double errorRate;

    @ApiModelProperty("进程启动时间")
    private LocalDateTime startedAt;

    @ApiModelProperty("最后心跳时间")
    private LocalDateTime lastHeartbeatAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
