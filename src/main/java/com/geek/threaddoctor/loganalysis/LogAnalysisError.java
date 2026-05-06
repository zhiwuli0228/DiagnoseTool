/**
 * Copyright &copy; 2026-2026 zhiwu Technologies Co., Ltd. All rights reserved.
 */

package com.geek.threaddoctor.loganalysis;

import java.time.LocalDateTime;

/**
 * 承载不可变业务数据。
 *
 * @author zhiwuli
 * @since 2026-05-07
 */
public record LogAnalysisError(String code, String message, String sourceFile, LocalDateTime occurredAt) {
    /**
     * 创建当前时间的错误信息。
     *
     * @param code 业务参数
     * @param message 消息内容
     * @param sourceFile 业务参数
     * @return 业务处理结果
     */
    public static LogAnalysisError now(String code, String message, String sourceFile) {
        return new LogAnalysisError(code, message, sourceFile, LocalDateTime.now());
    }
}
