package com.maotuanzi.ops;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 当前服务实例身份信息
 */
@Getter
@Component
public class OpsInstanceInfo {

    @Value("${spring.application.name:maotuanzi-api}")
    private String appName;

    @Value("${server.port:8080}")
    private Integer port;

    @Value("${spring.profiles.active:default}")
    private String profile;

    @Value("${ops.version:1.0.0}")
    private String version;

    private String instanceId;
    private String host;
    private String ip;
    private String pid;
    private LocalDateTime startedAt;

    @PostConstruct
    public void init() {
        try {
            InetAddress addr = InetAddress.getLocalHost();
            this.host = addr.getHostName();
            this.ip = addr.getHostAddress();
        } catch (Throwable t) {
            this.host = "unknown";
            this.ip = "127.0.0.1";
        }
        String rtName = ManagementFactory.getRuntimeMXBean().getName();
        int idx = rtName.indexOf('@');
        this.pid = idx > 0 ? rtName.substring(0, idx) : rtName;
        this.startedAt = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(ManagementFactory.getRuntimeMXBean().getStartTime()),
                ZoneId.systemDefault());
        this.instanceId = this.host + ":" + this.port + "#" + this.pid;
    }
}
