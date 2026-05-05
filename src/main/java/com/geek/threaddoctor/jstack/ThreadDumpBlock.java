package com.geek.threaddoctor.jstack;

import java.util.List;

public record ThreadDumpBlock(
        String name,
        String tid,
        String nid,
        Thread.State state,
        List<String> frames,
        List<String> locks) {
}
