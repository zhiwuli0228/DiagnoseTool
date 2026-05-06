/**
 * Copyright &copy; 2026-2026 zhiwu Technologies Co., Ltd. All rights reserved.
 */

package com.geek.threaddoctor.api;

import com.geek.threaddoctor.llm.LlmConfigurationStatus;
import com.geek.threaddoctor.llm.LlmConfigurationUpdateRequest;
import com.geek.threaddoctor.llm.LlmRuntimeConfigurationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 封装业务逻辑和数据处理能力。
 *
 * @author zhiwuli
 * @since 2026-05-07
 */
@RestController
@RequestMapping("/api/llm/configuration")
public class LlmConfigurationController {
    private final LlmRuntimeConfigurationService configurationService;

    /**
     * 执行业务操作。
     *
     * @param configurationService 业务服务依赖
     */
    public LlmConfigurationController(LlmRuntimeConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    @GetMapping
    LlmConfigurationStatus get() {
        return configurationService.status();
    }

    @PutMapping
    LlmConfigurationStatus save(@Valid @RequestBody LlmConfigurationUpdateRequest request) {
        return configurationService.save(request);
    }

    @DeleteMapping
    LlmConfigurationStatus clear() {
        return configurationService.clear();
    }
}
