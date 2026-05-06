/**
 * Copyright &copy; 2026-2026 zhiwu Technologies Co., Ltd. All rights reserved.
 */

package com.geek.threaddoctor.prompt;

import java.util.List;

/**
 * 封装业务逻辑和数据处理能力。
 *
 * @author zhiwuli
 * @since 2026-05-07
 */
public class MissingPromptVariableException extends PromptRenderException {
    private final List<String> missingVariables;

    /**
     * 判断业务条件是否成立。
     *
     * @param templateType 业务参数
     * @param missingVariables 业务参数
     */
    public MissingPromptVariableException(PromptTemplateType templateType, List<String> missingVariables) {
        super(templateType, "Missing prompt variables for " + templateType + ": " + missingVariables, null);
        this.missingVariables = List.copyOf(missingVariables);
    }

    /**
     * 获取缺失的提示词变量。
     *
     * @return 缺失的提示词变量
     */
    public List<String> missingVariables() {
        return missingVariables;
    }
}
