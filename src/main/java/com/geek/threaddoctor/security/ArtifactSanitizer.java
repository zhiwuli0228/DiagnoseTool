/**
 * Copyright &copy; 2026-2026 zhiwu Technologies Co., Ltd. All rights reserved.
 */

package com.geek.threaddoctor.security;

import com.geek.threaddoctor.loganalysis.LogAnalysisProperties;
import com.geek.threaddoctor.loganalysis.SensitiveDataMasker;
import org.springframework.stereotype.Component;

/**
 * 封装业务逻辑和数据处理能力。
 *
 * @author zhiwuli
 * @since 2026-05-07
 */
@Component
public class ArtifactSanitizer {
    private final SensitiveDataMasker masker;
    private final SecurityLimitsProperties limits;

    /**
     * 执行业务操作。
     *
     * @param masker 业务参数
     * @param limits 业务参数
     */
    public ArtifactSanitizer(SensitiveDataMasker masker, SecurityLimitsProperties limits) {
        this.masker = masker;
        this.limits = limits;
    }

    /**
     * 清理敏感或超长内容。
     *
     * @param text 待处理文本
     * @param properties 配置属性
     * @return 清理后的文本
     */
    public String sanitize(String text, LogAnalysisProperties properties) {
        return limit(masker.mask(text, properties), limits.artifactOutputMaxLength());
    }

    private String limit(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, Math.max(0, maxLength - 14)) + "...[truncated]";
    }
}
