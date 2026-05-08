/**
 * Copyright &copy; 2026-2026 zhiwu Technologies Co., Ltd. All rights reserved.
 */

package com.geek.threaddoctor.loganalysis;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Sidecar 本地运行配置。
 *
 * @author zhiwuli
 * @since 2026-05-08
 */
@ConfigurationProperties(prefix = "thread-doctor.sidecar")
public record SidecarProperties(
        int port,
        List<String> allowedOrigins,
        long maxParseMillis,
        int maxWorkers,
        int maxResultEvents) {
    public SidecarProperties {
        port = port <= 0 ? 18765 : port;
        allowedOrigins = allowedOrigins == null || allowedOrigins.isEmpty()
                ? List.of("http://localhost:5173", "http://127.0.0.1:5173", "http://localhost:8080", "http://127.0.0.1:8080")
                : List.copyOf(allowedOrigins);
        maxParseMillis = maxParseMillis <= 0 ? 10 * 60 * 1000L : maxParseMillis;
        maxWorkers = maxWorkers <= 0 ? 2 : maxWorkers;
        maxResultEvents = maxResultEvents <= 0 ? 200 : maxResultEvents;
    }
}
