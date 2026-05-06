package com.geek.threaddoctor.api;

import com.geek.threaddoctor.memory.IncidentCard;
import com.geek.threaddoctor.memory.IncidentCardService;
import jakarta.validation.constraints.Pattern;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

@RestController
@RequestMapping("/api/incidents/{sessionId}/incident-card")
@Validated
public class IncidentCardController {
    private final IncidentCardService service;

    public IncidentCardController(IncidentCardService service) {
        this.service = service;
    }

    @PostMapping
    IncidentCard generate(@PathVariable @Pattern(regexp = "INC-[A-Za-z0-9-]{1,80}") String sessionId) {
        return service.generate(sessionId);
    }

    @GetMapping
    IncidentCard latest(@PathVariable @Pattern(regexp = "INC-[A-Za-z0-9-]{1,80}") String sessionId) {
        return service.latest(sessionId);
    }
}
