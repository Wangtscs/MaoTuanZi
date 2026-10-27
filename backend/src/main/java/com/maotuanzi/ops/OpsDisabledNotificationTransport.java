package com.maotuanzi.ops;

import org.springframework.stereotype.Component;
import java.util.Map;

/** No outbound transport is installed; adding config alone never causes network traffic. */
@Component
public class OpsDisabledNotificationTransport implements OpsNotificationTransport {
    @Override
    public int send(String channel, Map<String, String> configuration, String payload) {
        throw new UnsupportedOperationException("未安装通知发送适配器");
    }
}
