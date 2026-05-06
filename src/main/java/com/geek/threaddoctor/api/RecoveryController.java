/**
 * Copyright &copy; 2026-2026 zhiwu Technologies Co., Ltd. All rights reserved.
 */

package com.geek.threaddoctor.api;

import com.geek.threaddoctor.recovery.RecoveryAction;
import com.geek.threaddoctor.recovery.RecoveryActionService;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

/**
 * 提供业务说明。
 *
 * @author zhiwuli
 * @since 2026-05-07
 */
@RestController
@RequestMapping("/api/incidents/{sessionId}/recovery-actions")
@Validated
public class RecoveryController {
    private final RecoveryActionService service;

    /**
     * 执行业务操作。
     *
     * @param service 业务服务
     */
    public RecoveryController(RecoveryActionService service) {
        this.service = service;
    }

    @PostMapping
    List<RecoveryAction> generate(@PathVariable @Pattern(regexp = "INC-[A-Za-z0-9-]{1,80}") String sessionId) {
        return service.generate(sessionId);
    }

    @PostMapping("/{actionId}/execute")
    RecoveryAction execute(@PathVariable @Pattern(regexp = "INC-[A-Za-z0-9-]{1,80}") String sessionId,
            @PathVariable @Pattern(regexp = "ACT-[A-Za-z0-9-]{1,80}") String actionId) {
        return service.simulate(sessionId, actionId);
    }
}
