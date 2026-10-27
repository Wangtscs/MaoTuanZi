package com.maotuanzi.ops;

import java.util.Map;

/** Integration boundary. Implementations must enforce a timeout; this release has no network sender. */
public interface OpsNotificationTransport {
    int send(String channel, Map<String, String> configuration, String payload) throws Exception;
}
