/**
 * Copyright &copy; 2026-2026 zhiwu Technologies Co., Ltd. All rights reserved.
 */

package com.geek.threaddoctor.prompt;

/**
 * 封装业务逻辑和数据处理能力。
 *
 * @author zhiwuli
 * @since 2026-05-07
 */
public class PromptTemplateNotFoundException extends PromptTemplateException {
    /**
     * 执行业务操作。
     *
     * @param templateType 业务参数
     * @param templatePath 业务参数
     */
    public PromptTemplateNotFoundException(PromptTemplateType templateType, String templatePath) {
        super(templateType, templatePath, "Prompt template not found: " + templateType + " at " + templatePath);
    }
}
