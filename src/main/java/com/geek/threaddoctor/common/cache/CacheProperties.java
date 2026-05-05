package com.geek.threaddoctor.common.cache;

import org.springframework.boot.context.properties.ConfigurationProperties;

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
