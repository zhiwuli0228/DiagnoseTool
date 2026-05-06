/**
 * Copyright &copy; 2026-2026 zhiwu Technologies Co., Ltd. All rights reserved.
 */

package com.geek.threaddoctor.common.cache;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * 封装业务逻辑和数据处理能力。
 *
 * @author zhiwuli
 * @since 2026-05-07
 */
public class InMemoryStore<T> {
    private final ConcurrentHashMap<String, Entry<T>> entries = new ConcurrentHashMap<>();
    private final int maxEntries;
    private final Duration ttl;
    private final Clock clock;

    /**
     * 执行业务操作。
     *
     * @param properties 配置属性
     */
    public InMemoryStore(CacheProperties properties) {
        this(properties, Clock.systemUTC());
    }

    InMemoryStore(CacheProperties properties, Clock clock) {
        this.maxEntries = properties.maxEntries();
        this.ttl = Duration.ofSeconds(properties.ttlSeconds());
        this.clock = clock;
    }

    /**
     * 保存业务记录。
     *
     * @param id 记录标识
     * @param value 待处理值
     * @return 业务处理结果
     */
    public T save(String id, T value) {
        cleanupExpired();
        entries.put(id, new Entry<>(value, Instant.now(clock)));
        evictOverflow();
        return value;
    }

    /**
     * 根据标识查找记录。
     *
     * @param id 记录标识
     * @return 可能存在的匹配记录
     */
    public Optional<T> findById(String id) {
        cleanupExpired();
        Entry<T> entry = entries.get(id);
        return entry == null ? Optional.empty() : Optional.of(entry.value());
    }

    /**
     * 查找满足条件的缓存值。
     *
     * @param predicate 过滤条件
     * @return 匹配的记录集合
     */
    public List<T> findBy(Predicate<T> predicate) {
        cleanupExpired();
        return entries.values().stream()
                .map(Entry::value)
                .filter(predicate)
                .toList();
    }

    /**
     * 查找满足条件的最新缓存值。
     *
     * @param predicate 过滤条件
     * @param orderKey 排序键提取器
     * @return 可能存在的匹配记录
     */
    public Optional<T> findLatest(Predicate<T> predicate, Function<T, ? extends Comparable<?>> orderKey) {
        cleanupExpired();
        return entries.values().stream()
                .map(Entry::value)
                .filter(predicate)
                .max(Comparator.comparing(orderKey, InMemoryStore::compareComparable));
    }

    private void cleanupExpired() {
        Instant now = Instant.now(clock);
        entries.entrySet().removeIf(entry -> Duration.between(entry.getValue().createdAt(), now).compareTo(ttl) > 0);
    }

    private void evictOverflow() {
        int overflow = entries.size() - maxEntries;
        if (overflow <= 0) {
            return;
        }
        entries.entrySet().stream()
                .sorted(Comparator.comparing(entry -> entry.getValue().createdAt()))
                .limit(overflow)
                .map(java.util.Map.Entry::getKey)
                .forEach(entries::remove);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static int compareComparable(Comparable left, Comparable right) {
        return left.compareTo(right);
    }

    private record Entry<T>(T value, Instant createdAt) {
    }
}
