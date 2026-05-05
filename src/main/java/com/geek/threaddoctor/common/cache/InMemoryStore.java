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

public class InMemoryStore<T> {
    private final ConcurrentHashMap<String, Entry<T>> entries = new ConcurrentHashMap<>();
    private final int maxEntries;
    private final Duration ttl;
    private final Clock clock;

    public InMemoryStore(CacheProperties properties) {
        this(properties, Clock.systemUTC());
    }

    InMemoryStore(CacheProperties properties, Clock clock) {
        this.maxEntries = properties.maxEntries();
        this.ttl = Duration.ofSeconds(properties.ttlSeconds());
        this.clock = clock;
    }

    public T save(String id, T value) {
        cleanupExpired();
        entries.put(id, new Entry<>(value, Instant.now(clock)));
        evictOverflow();
        return value;
    }

    public Optional<T> findById(String id) {
        cleanupExpired();
        Entry<T> entry = entries.get(id);
        return entry == null ? Optional.empty() : Optional.of(entry.value());
    }

    public List<T> findBy(Predicate<T> predicate) {
        cleanupExpired();
        return entries.values().stream()
                .map(Entry::value)
                .filter(predicate)
                .toList();
    }

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
