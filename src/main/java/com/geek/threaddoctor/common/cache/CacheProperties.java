/**
 * Copyright &copy; 2026-2026 zhiwu Technologies Co., Ltd. All rights reserved.
 */

package com.geek.threaddoctor.common.cache;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 承载不可变业务数据。
 *
 * @author zhiwuli
 * @since 2026-05-07
 */
@ConfigurationProperties(prefix = "thread-doctor.cache")
public record CacheProperties(int maxEntries, long ttlSeconds) {
    public CacheProperties {
        if (maxEntries <= 0) {
            maxEntries = 1000;
        }
        if (ttlSeconds <= 0) {
            ttlSeconds = 7200;
        }
    }
}
