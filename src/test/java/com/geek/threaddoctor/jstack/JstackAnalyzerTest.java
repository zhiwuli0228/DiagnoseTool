package com.geek.threaddoctor.jstack;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class JstackAnalyzerTest {
    private final JstackAnalyzer analyzer = new JstackAnalyzer();

    @Test
    void parsesThreadBlocksAndStateCounts() {
        String text = """
                "worker-1" #1 prio=5 os_prio=0 tid=0x1 nid=0x11 waiting on condition
                   java.lang.Thread.State: WAITING (parking)
                    at jdk.internal.misc.Unsafe.park(Native Method)

                "worker-2" #2 prio=5 os_prio=0 tid=0x2 nid=0x12 runnable
                   java.lang.Thread.State: RUNNABLE
                    at java.lang.Thread.run(Thread.java:1)
                """;

        JstackAnalysisResult result = analyzer.analyze(text);

        assertThat(result.totalThreads()).isEqualTo(2);
        assertThat(result.stateCount().get(Thread.State.WAITING)).isEqualTo(1);
        assertThat(result.stateCount().get(Thread.State.RUNNABLE)).isEqualTo(1);
        assertThat(result.threadGroups().get("worker")).isEqualTo(2);
    }

    @Test
    void detectsLockContentionAndRedisBlocking() {
        String text = """
                "cache-refresh-worker-1" #1 prio=5 os_prio=0 tid=0x1 nid=0x11 runnable
                   java.lang.Thread.State: RUNNABLE
                    at redis.clients.jedis.util.RedisInputStream.ensureFill(RedisInputStream.java:1)
                    - waiting to lock <0xabc>

                "cache-refresh-worker-2" #2 prio=5 os_prio=0 tid=0x2 nid=0x12 waiting
                   java.lang.Thread.State: WAITING (parking)
                    at redis.clients.jedis.Protocol.process(Protocol.java:1)
                    - waiting to lock <0xabc>
                """;

        JstackAnalysisResult result = analyzer.analyze(text);

        assertThat(result.lockContentions()).hasSize(1);
        assertThat(result.lockContentions().getFirst().lockId()).isEqualTo("0xabc");
        assertThat(result.suspiciousThreads()).extracting(SuspiciousThread::reason).contains("REDIS_IO_BLOCKED");
    }
}
