/**
 * Copyright &copy; 2026-2026 zhiwu Technologies Co., Ltd. All rights reserved.
 */

package com.geek.threaddoctor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * 封装业务逻辑和数据处理能力。
 *
 * @author zhiwuli
 * @since 2026-05-07
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class ThreadDoctorApplication {
    /**
     * 启动应用。
     *
     * @param args 应用启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(ThreadDoctorApplication.class, args);
    }
}
