package com.maotuanzi.ops;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maotuanzi.entity.*;
import com.maotuanzi.mapper.OpsMapper;
import com.maotuanzi.service.impl.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class OpsEnterpriseReliabilityTest {
    @Test
    void collectorFailureKeepsUnknownAndOtherSourcesAvailable() {
        OpsSystemMetrics system = mock(OpsSystemMetrics.class);
        OpsHttpMetrics http = mock(OpsHttpMetrics.class);
        OpsMapper mapper = mock(OpsMapper.class);
        when(system.jvm()).thenThrow(new IllegalStateException("unavailable"));
        when(system.os()).thenReturn(Map.of("processCpuLoad", -1));
        when(system.disk()).thenReturn(Map.of("freePct", 42));
        when(mapper.countAiTasks(any(), any())).thenThrow(new IllegalStateException("database unavailable"));
        Map<String, Double> values = new OpsMetricRegistry(http, system, mock(OpsLogBuffer.class), mapper).collect();
        assertTrue(values.containsKey("ai.task.pending.count"));
        assertNull(values.get("ai.task.pending.count"));
        assertNull(values.get("jvm.heap.used.pct"));
        assertNull(values.get("system.cpu.load"));
        assertNull(values.get("redis.available"));
        assertEquals(42.0, values.get("system.disk.free.pct"));
    }

    @Test
    void unknownAndNonFiniteSampleNeverResolveExistingAlarm() {
        OpsMapper mapper = mock(OpsMapper.class);
        OpsMetricRegistry registry = mock(OpsMetricRegistry.class);
        OpsAlarmRule rule = new OpsAlarmRule(); rule.setId(1L); rule.setMetricKey("ai.task.pending.count");
        rule.setFiring(1); rule.setThreshold(10.0); rule.setOperator("gt");
        when(mapper.selectAlarmRules(null, 1)).thenReturn(List.of(rule));
        OpsAlarmServiceImpl alarm = new OpsAlarmServiceImpl(mapper, registry, mock(OpsNotificationService.class));
        when(registry.collect()).thenReturn(Collections.singletonMap(rule.getMetricKey(), null));
        assertEquals(0, alarm.evaluate());
        when(registry.collect()).thenReturn(Map.of(rule.getMetricKey(), Double.NaN));
        assertEquals(0, alarm.evaluate());
        verify(mapper, never()).resolveAlarmRecord(any(), any(), anyInt(), any());
        verify(mapper, never()).updateAlarmRuleCheck(any(), any(), any(), any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void healthSeparatesInfrastructureFromBusinessAndUnknownScore() {
        OpsMetricRegistry registry = mock(OpsMetricRegistry.class);
        when(registry.collect()).thenReturn(Map.of("system.disk.free.pct", 50.0));
        OpsSystemMetrics system = mock(OpsSystemMetrics.class);
        when(system.dbPing()).thenThrow(new IllegalStateException());
        when(system.redis()).thenReturn(Map.of("ok", true, "latencyMs", 1));
        OpsServiceImpl service = new OpsServiceImpl(mock(OpsMapper.class), mock(OpsHttpMetrics.class), system,
                registry, mock(OpsLogBuffer.class), mock(OpsLogAppender.class), mock(OpsInstanceInfo.class), mock(OpsConfigHistory.class));
        Map<String, Object> result = service.health();
        assertEquals("UNKNOWN", result.get("status"));
        assertNull(result.get("score"));
        assertNotNull(result.get("timestamp"));
        assertEquals("UNKNOWN", ((Map<String, Object>) result.get("business")).get("status"));
        assertTrue(result.containsKey("infrastructure"));
    }

    private OpsMapper memoryMapper(Map<String, SystemConfig> records) {
        OpsMapper mapper = mock(OpsMapper.class);
        when(mapper.selectOpsConfigs()).thenAnswer(a -> new ArrayList<>(records.values()));
        when(mapper.selectOpsConfigForUpdate(anyString())).thenAnswer(a -> records.get(a.getArgument(0)));
        when(mapper.upsertOpsConfig(anyString(), nullable(String.class), nullable(String.class), nullable(String.class))).thenAnswer(a -> {
            SystemConfig c = new SystemConfig(); c.setConfigKey(a.getArgument(0)); c.setConfigValue(a.getArgument(1));
            c.setDescription(a.getArgument(2)); c.setUpdatedBy(a.getArgument(3)); records.put(c.getConfigKey(), c); return 1;
        });
        when(mapper.deleteOpsConfig(anyString())).thenAnswer(a -> records.remove(a.getArgument(0)) == null ? 0 : 1);
        when(mapper.restoreOpsConfig(anyString(), nullable(String.class), nullable(String.class), nullable(String.class))).thenAnswer(a -> {
            SystemConfig c = new SystemConfig(); c.setConfigKey(a.getArgument(0)); c.setConfigValue(a.getArgument(1));
            c.setDescription(a.getArgument(2)); c.setUpdatedBy(a.getArgument(3)); records.put(c.getConfigKey(), c); return 1;
        });
        return mapper;
    }

    @Test
    void configMaskPreservesSecretWithoutWritingItToHistory() {
        Map<String, SystemConfig> records = new HashMap<>();
        OpsMapper mapper = memoryMapper(records);
        OpsConfigHistory history = new OpsConfigHistory(mapper, new ObjectMapper());
        history.save("ops.api.token", "test-secret", "credential", "admin#1");
        history.save("ops.api.token", OpsConfigHistory.MASK, "new description", "admin#1");
        assertEquals("test-secret", records.get("ops.api.token").getConfigValue());
        for (Map<String, Object> event : history.history("ops.api.token")) {
            assertEquals(false, event.get("canRollback"));
            assertFalse(event.toString().contains("test-secret"));
        }
        assertThrows(IllegalArgumentException.class, () -> history.rollback("ops.api.token", UUID.randomUUID().toString()));
        assertThrows(IllegalArgumentException.class, () -> history.save("ops.internal.bad", "x", null, "admin#1"));
    }

    @Test
    void configRollbackUsesRealPreviousRevisionAndCapturesRollback() {
        Map<String, SystemConfig> records = new HashMap<>();
        OpsConfigHistory history = new OpsConfigHistory(memoryMapper(records), new ObjectMapper());
        history.save("ops.retention.days", "7", null, "admin#1");
        history.save("ops.retention.days", "30", null, "admin#1");
        Map<String, Object> change = history.history("ops.retention.days").stream()
                .filter(e -> "7".equals(e.get("previousValue"))).findFirst().orElseThrow();
        history.rollback("ops.retention.days", (String) change.get("revision"));
        assertEquals("7", records.get("ops.retention.days").getConfigValue());
        assertTrue(history.history("ops.retention.days").stream().anyMatch(e -> "ROLLBACK".equals(e.get("action"))));
    }

    private SystemConfig config(String key, String value) {
        SystemConfig c = new SystemConfig(); c.setConfigKey(key); c.setConfigValue(value); return c;
    }
    private void fire(OpsNotificationService notifications) {
        OpsAlarmRule rule = new OpsAlarmRule(); rule.setChannels("webhook");
        OpsAlarmRecord alarm = new OpsAlarmRecord(); alarm.setId(7L); alarm.setMessage("pending threshold exceeded"); alarm.setLevel("P2");
        notifications.notify(rule, alarm);
    }

    @Test
    void defaultNotificationTransportNeverSendsEvenWithConfiguration() {
        Map<String, SystemConfig> records = new HashMap<>();
        records.put("ops.notify.enabled", config("ops.notify.enabled", "true"));
        records.put("ops.notify.webhook.url", config("ops.notify.webhook.url", "https://example.invalid"));
        OpsNotificationService notifications = new OpsNotificationService(memoryMapper(records), new ObjectMapper(), new OpsDisabledNotificationTransport());
        fire(notifications);
        assertEquals("UNCONFIGURED", notifications.receipts().get(0).get("status"));
        assertEquals(0, notifications.receipts().get(0).get("attempts"));
    }

    @Test
    void mockFailureCanRetryAndAcceptanceIsNotFinalDelivery() throws Exception {
        Map<String, SystemConfig> records = new HashMap<>();
        records.put("ops.notify.enabled", config("ops.notify.enabled", "true"));
        records.put("ops.notify.webhook.url", config("ops.notify.webhook.url", "https://example.invalid"));
        OpsNotificationTransport transport = mock(OpsNotificationTransport.class);
        when(transport.send(anyString(), anyMap(), anyString())).thenThrow(new java.io.IOException()).thenReturn(202);
        OpsNotificationService notifications = new OpsNotificationService(memoryMapper(records), new ObjectMapper(), transport);
        fire(notifications);
        Map<String, Object> failed = notifications.receipts().get(0);
        assertEquals("FAILED", failed.get("status")); assertEquals(true, failed.get("retryable"));
        Map<String, Object> accepted = notifications.retry((String) failed.get("id"));
        assertEquals("ACCEPTED", accepted.get("status")); assertEquals(2, accepted.get("attempts"));
        assertTrue(String.valueOf(accepted.get("detail")).contains("未证明最终"));
        assertThrows(IllegalArgumentException.class, () -> notifications.retry((String) failed.get("id")));
    }

    @Test
    void persistenceFailureIsNotReportedAsSuccess() {
        OpsMapper mapper = mock(OpsMapper.class);
        OpsConfigHistory history = new OpsConfigHistory(mapper, new ObjectMapper());
        assertThrows(IllegalStateException.class, () -> history.save("ops.retention.days", "7", null, "admin#1"));
    }

    @Test
    void notificationRetryHasAnAttemptLimit() throws Exception {
        Map<String, SystemConfig> records = new HashMap<>();
        records.put("ops.notify.enabled", config("ops.notify.enabled", "true"));
        records.put("ops.notify.webhook.url", config("ops.notify.webhook.url", "https://example.invalid"));
        OpsNotificationTransport transport = mock(OpsNotificationTransport.class);
        when(transport.send(anyString(), anyMap(), anyString())).thenReturn(503);
        OpsNotificationService notifications = new OpsNotificationService(memoryMapper(records), new ObjectMapper(), transport);
        fire(notifications);
        String id = (String) notifications.receipts().get(0).get("id");
        notifications.retry(id);
        Map<String, Object> last = notifications.retry(id);
        assertEquals(3, last.get("attempts")); assertEquals(false, last.get("retryable"));
        assertThrows(IllegalArgumentException.class, () -> notifications.retry(id));
        verify(transport, times(3)).send(anyString(), anyMap(), anyString());
    }

    @Test
    void notificationTransportIsBoundedEvenWhenAdapterBlocks() {
        Map<String, SystemConfig> records = new HashMap<>();
        records.put("ops.notify.enabled", config("ops.notify.enabled", "true"));
        records.put("ops.notify.webhook.url", config("ops.notify.webhook.url", "https://example.invalid"));
        OpsNotificationTransport blocking = (channel, configuration, payload) -> { Thread.sleep(30_000); return 200; };
        OpsNotificationService notifications = new OpsNotificationService(memoryMapper(records), new ObjectMapper(), blocking);
        assertTimeout(java.time.Duration.ofSeconds(5), () -> fire(notifications));
        Map<String, Object> receipt = notifications.receipts().get(0);
        assertEquals("FAILED", receipt.get("status"));
        assertTrue(String.valueOf(receipt.get("detail")).contains("TimeoutException"));
    }
}
