package com.geek.threaddoctor.jstack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
// jstack 分析器提取线程状态、热点调用栈、锁竞争和常见外部 IO 阻塞线索。
public class JstackAnalyzer {
    private static final Pattern HEADER = Pattern.compile("^\"([^\"]+)\".*tid=([^\\s]+).*nid=([^\\s]+).*");
    private static final Pattern STATE = Pattern.compile("^\\s+java\\.lang\\.Thread\\.State:\\s+([A-Z_]+).*");
    private static final Pattern LOCK = Pattern.compile(".*<(0x[0-9a-fA-F]+)>.*");
    // 这些关键词对应排障中高频的阻塞点，用于从调用栈中筛出可疑线程。
    private static final List<String> BLOCKING_KEYWORDS = List.of(
            "RedisInputStream", "redis.clients.jedis", "java.sql", "jdbc", "kafka", "SocketInputStream",
            "socketRead", "HttpClient", "FileInputStream");

    public JstackAnalysisResult analyze(String text) {
        List<ThreadDumpBlock> threads = parse(text);
        Map<Thread.State, Integer> states = new EnumMap<>(Thread.State.class);
        Map<String, Integer> groups = new LinkedHashMap<>();
        Map<String, List<String>> lockWaiters = new HashMap<>();
        Map<String, Integer> hotStacks = new HashMap<>();
        List<SuspiciousThread> suspicious = new ArrayList<>();

        for (ThreadDumpBlock thread : threads) {
            states.merge(thread.state(), 1, Integer::sum);
            groups.merge(groupName(thread.name()), 1, Integer::sum);
            // 同一锁对象被多个线程引用时，优先认为存在锁竞争风险。
            for (String lock : thread.locks()) {
                lockWaiters.computeIfAbsent(lock, ignored -> new ArrayList<>()).add(thread.name());
            }
            String signature = thread.frames().stream().limit(3).collect(Collectors.joining("\n"));
            if (!signature.isBlank()) {
                // 取前三帧作为近似签名，足以识别大量线程卡在同一业务入口或中间件调用。
                hotStacks.merge(signature, 1, Integer::sum);
            }
            thread.frames().stream()
                    .filter(frame -> BLOCKING_KEYWORDS.stream().anyMatch(k -> frame.toLowerCase().contains(k.toLowerCase())))
                    .findFirst()
                    .ifPresent(frame -> suspicious.add(new SuspiciousThread(thread.name(), classify(frame), frame)));
        }

        List<LockContention> contentions = lockWaiters.entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .map(entry -> new LockContention(entry.getKey(), entry.getValue().size(), entry.getValue()))
                .toList();
        List<HotStack> hot = hotStacks.entrySet().stream()
                .filter(entry -> entry.getValue() > 1)
                .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder()))
                .map(entry -> new HotStack(entry.getKey(), entry.getValue()))
                .toList();
        boolean deadlock = text != null && text.toLowerCase().contains("deadlock");
        // jstack 标准输出会包含 deadlock 标记，MVP 先用文本标记兜底识别死锁。
        List<String> deadlockDetails = deadlock ? List.of("jstack text contains deadlock marker") : List.of();
        return new JstackAnalysisResult(threads.size(), states, groups, contentions, hot, suspicious, deadlock, deadlockDetails, threads);
    }

    public List<ThreadDumpBlock> parse(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        List<ThreadDumpBlock> result = new ArrayList<>();
        String[] lines = text.replace("\r\n", "\n").split("\n");
        String name = null;
        String tid = null;
        String nid = null;
        Thread.State state = Thread.State.RUNNABLE;
        List<String> frames = new ArrayList<>();
        List<String> locks = new ArrayList<>();

        for (String line : lines) {
            Matcher header = HEADER.matcher(line);
            if (header.matches()) {
                // 遇到新线程头时，先封存上一段线程块，再开始收集当前线程。
                if (name != null) {
                    result.add(new ThreadDumpBlock(name, tid, nid, state, List.copyOf(frames), List.copyOf(locks)));
                }
                name = header.group(1);
                tid = header.group(2);
                nid = header.group(3);
                state = Thread.State.RUNNABLE;
                frames = new ArrayList<>();
                locks = new ArrayList<>();
                continue;
            }
            if (name == null) {
                continue;
            }
            Matcher stateMatcher = STATE.matcher(line);
            if (stateMatcher.matches()) {
                state = Thread.State.valueOf(stateMatcher.group(1));
            }
            if (line.trim().startsWith("at ")) {
                frames.add(line.trim());
            }
            Matcher lockMatcher = LOCK.matcher(line);
            if (lockMatcher.matches()) {
                // 记录锁对象地址，后续按地址聚合等待线程。
                locks.add(lockMatcher.group(1));
            }
        }
        if (name != null) {
            result.add(new ThreadDumpBlock(name, tid, nid, state, List.copyOf(frames), List.copyOf(locks)));
        }
        return result;
    }

    private String groupName(String threadName) {
        // 线程池线程通常以 pool-name-序号 命名，去掉尾部序号可观察线程池规模。
        int lastDash = threadName.lastIndexOf('-');
        return lastDash > 0 ? threadName.substring(0, lastDash) : threadName;
    }

    private String classify(String frame) {
        String lower = frame.toLowerCase();
        // 分类结果会被故障模式检测器消费，因此保持为稳定的内部枚举式字符串。
        if (lower.contains("redis") || lower.contains("jedis")) {
            return "REDIS_IO_BLOCKED";
        }
        if (lower.contains("kafka")) {
            return "KAFKA_BLOCKED";
        }
        if (lower.contains("jdbc") || lower.contains("java.sql")) {
            return "DB_IO_BLOCKED";
        }
        return "IO_BLOCKED";
    }
}
