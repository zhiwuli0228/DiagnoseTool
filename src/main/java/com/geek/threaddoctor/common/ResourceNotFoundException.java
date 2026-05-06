/**
 * Copyright &copy; 2026-2026 zhiwu Technologies Co., Ltd. All rights reserved.
 */

package com.geek.threaddoctor.common;

/**
 * 封装业务逻辑和数据处理能力。
 *
 * @author zhiwuli
 * @since 2026-05-07
 */
public class ResourceNotFoundException extends RuntimeException {
    /**
     * 执行业务操作。
     *
     * @param message 消息内容
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
