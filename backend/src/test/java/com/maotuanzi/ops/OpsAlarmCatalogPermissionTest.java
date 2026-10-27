package com.maotuanzi.ops;

import com.maotuanzi.annotation.RequireAdmin;
import com.maotuanzi.controller.ops.OpsAlarmController;
import com.maotuanzi.controller.ops.OpsMonitorController;
import com.maotuanzi.mapper.OpsMapper;
import com.maotuanzi.service.OpsAlarmService;
import com.maotuanzi.service.impl.OpsServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OpsAlarmCatalogPermissionTest {
    @Test
    void catalogUsesAlarmPermissionWithoutExpandingMonitorAccess() throws Exception {
        assertEquals("ops.alarm", OpsAlarmController.class.getAnnotation(RequireAdmin.class).permission());
        var method = OpsAlarmController.class.getMethod("metricCatalog");
        assertEquals("ops.alarm", method.getAnnotation(RequireAdmin.class).permission());
        assertArrayEquals(new String[]{"/metric-catalog"}, method.getAnnotation(GetMapping.class).value());
        assertEquals("ops.metrics", OpsMonitorController.class.getAnnotation(RequireAdmin.class).permission());
    }

    @Test
    void catalogReturnsOnlyMetadataAndNeverCollectsRuntimeMetrics() {
        OpsMetricRegistry registry = spy(new OpsMetricRegistry(mock(OpsHttpMetrics.class), mock(OpsSystemMetrics.class),
                mock(OpsLogBuffer.class), mock(OpsMapper.class)));
        OpsServiceImpl service = new OpsServiceImpl(mock(OpsMapper.class), mock(OpsHttpMetrics.class), mock(OpsSystemMetrics.class),
                registry, mock(OpsLogBuffer.class), mock(OpsLogAppender.class), mock(OpsInstanceInfo.class), mock(OpsConfigHistory.class));
        var result = new OpsAlarmController(mock(OpsAlarmService.class), service).metricCatalog();
        assertTrue(result.isSuccess());
        assertFalse(result.getData().isEmpty());
        for (Map<String, Object> entry : result.getData()) {
            assertEquals(Set.of("key", "name", "unit", "snapshot"), entry.keySet());
        }
        verify(registry).catalog();
        verify(registry, never()).collect();
    }
}
