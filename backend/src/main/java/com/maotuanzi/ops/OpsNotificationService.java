package com.maotuanzi.ops;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maotuanzi.entity.*;
import com.maotuanzi.mapper.OpsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

/** Receipts reuse private system_configs rows; the default transport cannot send notifications. */
@Component
@RequiredArgsConstructor
public class OpsNotificationService {
    private static final String PREFIX = OpsConfigHistory.PREFIX + "n.";
    private final OpsMapper mapper;
    private final ObjectMapper json;
    private final OpsNotificationTransport transport;
    private static final ExecutorService DELIVERY_POOL = new ThreadPoolExecutor(2, 2, 0, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(5), r -> { Thread t = new Thread(r, "ops-notification"); t.setDaemon(true); return t; },
            new ThreadPoolExecutor.AbortPolicy());

    public void notify(OpsAlarmRule rule, OpsAlarmRecord alarm) {
        String channels = rule.getChannels();
        if (channels == null || channels.isBlank()) channels = "unconfigured";
        String[] names = channels.replace("[", "").replace("]", "").replace("\"", "").split(",");
        for (int i = 0; i < Math.min(names.length, 5); i++) {
            Map<String, Object> receipt = new LinkedHashMap<>();
            receipt.put("id", UUID.randomUUID().toString()); receipt.put("alarmId", alarm.getId());
            receipt.put("channel", names[i].trim()); receipt.put("attempts", 0);
            receipt.put("message", alarm.getMessage()); receipt.put("level", alarm.getLevel());
            receipt.put("createdAt", Instant.now().toString());
            deliver(receipt);
        }
    }

    private void persist(Map<String, Object> receipt) {
        try {
            if (mapper.upsertOpsConfig(PREFIX + receipt.get("id"), json.writeValueAsString(receipt), "告警通知回执", "ops-alarm") < 1)
                throw new IllegalStateException("通知回执保存失败");
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) { throw new IllegalStateException("通知回执编码失败", e); }
    }

    private void deliver(Map<String, Object> receipt) {
        receipt.put("updatedAt", Instant.now().toString());
        Map<String, String> config = new HashMap<>();
        List<SystemConfig> configs = mapper.selectOpsConfigs();
        if (configs != null) configs.forEach(c -> { if (!c.getConfigKey().startsWith(OpsConfigHistory.PREFIX)) config.put(c.getConfigKey(), c.getConfigValue()); });
        String channel = String.valueOf(receipt.get("channel"));
        receipt.put("retryable", false);
        if (!channel.matches("[a-zA-Z0-9_-]{1,32}") || !"true".equalsIgnoreCase(config.get("ops.notify.enabled"))
                || config.get("ops.notify." + channel + ".url") == null || transport instanceof OpsDisabledNotificationTransport) {
            receipt.put("status", "UNCONFIGURED"); receipt.put("detail", "通知未启用、渠道地址未配置或发送适配器未安装");
            persist(receipt); return;
        }
        int attempts = ((Number) receipt.get("attempts")).intValue() + 1;
        receipt.put("attempts", attempts); receipt.put("status", "SENDING");
        receipt.put("detail", "发送中，进程中断时需核对接收端再重试");
        persist(receipt);
        try {
            String payload = json.writeValueAsString(Map.of("deliveryId", receipt.get("id"),
                    "message", String.valueOf(receipt.get("message")), "level", String.valueOf(receipt.get("level"))));
            Map<String, String> channelConfiguration = new HashMap<>();
            config.forEach((key, value) -> { if (key.startsWith("ops.notify." + channel + ".")) channelConfiguration.put(key, value); });
            Future<Integer> delivery = DELIVERY_POOL.submit(() -> transport.send(channel, Collections.unmodifiableMap(channelConfiguration), payload));
            int code;
            try { code = delivery.get(3, TimeUnit.SECONDS); }
            finally { if (!delivery.isDone()) delivery.cancel(true); }
            receipt.put("httpStatus", code);
            receipt.put("status", code >= 200 && code < 300 ? "ACCEPTED" : "FAILED");
            receipt.put("detail", code >= 200 && code < 300 ? "接收端已接受，未证明最终人员送达" : "接收端返回非成功状态");
        } catch (Exception e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            receipt.put("status", "FAILED"); receipt.put("detail", "发送失败: " + e.getClass().getSimpleName());
        }
        receipt.put("retryable", "FAILED".equals(receipt.get("status")) && attempts < 3);
        receipt.put("updatedAt", Instant.now().toString());
        persist(receipt);
    }

    public List<Map<String, Object>> receipts() {
        List<Map<String, Object>> result = new ArrayList<>();
        List<SystemConfig> all = mapper.selectOpsConfigs();
        if (all == null) return result;
        for (SystemConfig c : all) if (c.getConfigKey().startsWith(PREFIX)) {
            try { result.add(json.readValue(c.getConfigValue(), new TypeReference<Map<String, Object>>() {})); }
            catch (Exception e) { throw new IllegalStateException("通知回执损坏", e); }
        }
        result.sort(Comparator.comparing(x -> String.valueOf(x.get("createdAt")), Comparator.reverseOrder()));
        return result.stream().limit(200).collect(java.util.stream.Collectors.toList());
    }

    public synchronized Map<String, Object> retry(String id) {
        if (id == null || !id.matches("[a-fA-F0-9-]{36}")) throw new IllegalArgumentException("无效通知编号");
        Map<String, Object> r = receipts().stream().filter(x -> id.equals(x.get("id"))).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("通知回执不存在"));
        if ("ACCEPTED".equals(r.get("status")) || ((Number) r.get("attempts")).intValue() >= 3)
            throw new IllegalArgumentException("通知已被接收或重试次数已达上限");
        deliver(r); return r;
    }
}
