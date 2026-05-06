/**
 * Copyright &copy; 2026-2026 zhiwu Technologies Co., Ltd. All rights reserved.
 */

package com.geek.threaddoctor.jstack;

import java.util.List;

/**
 * 承载不可变业务数据。
 *
 * @author zhiwuli
 * @since 2026-05-07
 */
public record ThreadDumpBlock(
        String name,
        String tid,
        String nid,
        Thread.State state,
        List<String> frames,
        List<String> locks) {
}
