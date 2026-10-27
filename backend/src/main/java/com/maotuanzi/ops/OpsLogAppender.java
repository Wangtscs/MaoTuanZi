package com.maotuanzi.ops;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.AppenderBase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * 把运行期日志灌入 {@link OpsLogBuffer}，供运维平台日志中心实时查询。
 *
 * <p>通过代码方式挂载到 Logback ROOT Logger，无需改动 logback 配置文件。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OpsLogAppender {

    private final OpsLogBuffer logBuffer;

    /** 采集的最低级别 */
    @Value("${ops.log.capture-level:INFO}")
    private String captureLevel;

    private BufferAppender appender;

    @PostConstruct
    public void install() {
        try {
            LoggerFactory.getILoggerFactory();
            Object factory = LoggerFactory.getILoggerFactory();
            if (!(factory instanceof LoggerContext)) {
                log.warn("[OPS] 当前日志实现非 Logback，日志中心采集未启用");
                return;
            }
            LoggerContext ctx = (LoggerContext) factory;
            appender = new BufferAppender(logBuffer, Level.toLevel(captureLevel, Level.INFO));
            appender.setContext(ctx);
            appender.setName("OPS_BUFFER");
            appender.start();
            ctx.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME).addAppender(appender);
            log.info("[OPS] 日志中心采集已启用，级别 >= {}", appender.threshold);
        } catch (Throwable t) {
            log.warn("[OPS] 日志中心采集启用失败: {}", t.getMessage());
        }
    }

    @PreDestroy
    public void uninstall() {
        try {
            if (appender != null) {
                Object factory = LoggerFactory.getILoggerFactory();
                if (factory instanceof LoggerContext) {
                    ((LoggerContext) factory).getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME).detachAppender(appender);
                }
                appender.stop();
            }
        } catch (Throwable ignored) {
            // 关闭阶段忽略
        }
    }

    /** 动态调整采集级别 */
    public void setThreshold(String level) {
        if (appender != null) {
            appender.threshold = Level.toLevel(level, Level.INFO);
        }
        this.captureLevel = level;
    }

    public String getThreshold() {
        return appender == null ? captureLevel : appender.threshold.toString();
    }

    private static class BufferAppender extends AppenderBase<ILoggingEvent> {

        private final OpsLogBuffer buffer;
        private volatile Level threshold;

        BufferAppender(OpsLogBuffer buffer, Level threshold) {
            this.buffer = buffer;
            this.threshold = threshold;
        }

        @Override
        protected void append(ILoggingEvent event) {
            try {
                if (!event.getLevel().isGreaterOrEqual(threshold)) return;
                String stack = null;
                IThrowableProxy tp = event.getThrowableProxy();
                if (tp != null) {
                    StringBuilder sb = new StringBuilder(tp.getClassName());
                    if (tp.getMessage() != null) sb.append(": ").append(tp.getMessage());
                    sb.append('\n');
                    StackTraceElementProxy[] arr = tp.getStackTraceElementProxyArray();
                    int limit = Math.min(arr == null ? 0 : arr.length, 30);
                    for (int i = 0; i < limit; i++) {
                        sb.append("\tat ").append(arr[i].getSTEAsString()).append('\n');
                    }
                    if (arr != null && arr.length > limit) {
                        sb.append("\t... ").append(arr.length - limit).append(" more\n");
                    }
                    stack = sb.toString();
                    if (stack.length() > 8000) stack = stack.substring(0, 8000) + "...";
                    if (stack.isEmpty()) stack = ThrowableProxyUtil.asString(tp);
                }
                String traceId = event.getMDCPropertyMap() == null ? null : event.getMDCPropertyMap().get("traceId");
                buffer.append(event.getTimeStamp(), event.getLevel().toString(), event.getLoggerName(),
                        event.getThreadName(), traceId, event.getFormattedMessage(), stack);
            } catch (Throwable ignored) {
                // 日志采集不得影响业务
            }
        }
    }
}
