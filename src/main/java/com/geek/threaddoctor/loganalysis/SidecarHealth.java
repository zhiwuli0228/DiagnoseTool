/**
 * Copyright &copy; 2026-2026 zhiwu Technologies Co., Ltd. All rights reserved.
 */

package com.geek.threaddoctor.loganalysis;

import java.util.List;
import java.util.Map;

/**
 * Sidecar 健康检查响应。
 *
 * @author zhiwuli
 * @since 2026-05-08
 */
public record SidecarHealth(
        String status,
        String version,
        int port,
        List<String> capabilities,
        Map<String, Object> limits) {
}
