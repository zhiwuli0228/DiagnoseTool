package com.geek.threaddoctor.api;

import com.geek.threaddoctor.memory.IncidentCard;
import com.geek.threaddoctor.memory.IncidentCardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/incidents/{sessionId}/incident-card")
public class IncidentCardController {
    private final IncidentCardService service;

    public IncidentCardController(IncidentCardService service) {
        this.service = service;
    }

    @PostMapping
    IncidentCard generate(@PathVariable String sessionId) {
        return service.generate(sessionId);
    }

    @GetMapping
    IncidentCard latest(@PathVariable String sessionId) {
        return service.latest(sessionId);
    }
}
