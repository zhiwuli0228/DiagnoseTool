package com.geek.threaddoctor.api;

import com.geek.threaddoctor.recovery.RecoveryAction;
import com.geek.threaddoctor.recovery.RecoveryActionService;
import java.util.List;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/incidents/{sessionId}/recovery-actions")
public class RecoveryController {
    private final RecoveryActionService service;

    public RecoveryController(RecoveryActionService service) {
        this.service = service;
    }

    @PostMapping
    List<RecoveryAction> generate(@PathVariable String sessionId) {
        return service.generate(sessionId);
    }

    @PostMapping("/{actionId}/execute")
    RecoveryAction execute(@PathVariable String sessionId, @PathVariable String actionId) {
        return service.simulate(sessionId, actionId);
    }
}
