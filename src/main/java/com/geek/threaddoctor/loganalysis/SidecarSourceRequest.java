/**
 * Copyright &copy; 2026-2026 zhiwu Technologies Co., Ltd. All rights reserved.
 */

package com.geek.threaddoctor.loganalysis;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Sidecar 本地日志源请求。
 *
 * @author zhiwuli
 * @since 2026-05-08
 */
public record SidecarSourceRequest(
        @NotBlank
        @Size(max = 2048)
        String path) {
}
