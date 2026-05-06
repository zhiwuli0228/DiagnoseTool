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
public class PromptTemplateException extends RuntimeException {
    private final PromptTemplateType templateType;
    private final String templatePath;

    /**
     * 执行业务操作。
     *
     * @param templateType 业务参数
     * @param templatePath 业务参数
     * @param message 消息内容
     */
    public PromptTemplateException(PromptTemplateType templateType, String templatePath, String message) {
        super(message);
        this.templateType = templateType;
        this.templatePath = templatePath;
    }

    /**
     * 执行业务操作。
     *
     * @param templateType 业务参数
     * @param templatePath 业务参数
     * @param message 消息内容
     * @param cause 根因异常
     */
    public PromptTemplateException(PromptTemplateType templateType, String templatePath, String message, Throwable cause) {
        super(message, cause);
        this.templateType = templateType;
        this.templatePath = templatePath;
    }

    /**
     * 获取提示词模板类型。
     *
     * @return 业务处理结果
     */
    public PromptTemplateType templateType() {
        return templateType;
    }

    /**
     * 获取提示词模板路径。
     *
     * @return 文本结果
     */
    public String templatePath() {
        return templatePath;
    }
}
