package com.geek.threaddoctor.common.cache;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class InMemoryStoreTest {
    @Test
    void evictsOldestEntryWhenCapacityIsExceeded() {
        InMemoryStore<String> store = new InMemoryStore<>(new CacheProperties(1, 60));

        store.save("first", "first");
        store.save("second", "second");

        assertThat(store.findById("first")).isEmpty();
        assertThat(store.findById("second")).contains("second");
    }

    @Test
    void hidesExpiredEntries() {
        MutableClock clock = new MutableClock(Instant.parse("2026-05-05T00:00:00Z"));
        InMemoryStore<String> store = new InMemoryStore<>(new CacheProperties(10, 1), clock);

        store.save("item", "value");
        clock.advance(Duration.ofSeconds(2));

        assertThat(store.findById("item")).isEmpty();
    }

    private static final class MutableClock extends Clock {
        private Instant instant;

        private MutableClock(Instant instant) {
            this.instant = instant;
        }

        void advance(Duration duration) {
            instant = instant.plus(duration);
        }

        @Override
        public ZoneOffset getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}
