package com.example.videostreaming.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Health check controller.
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    /**
     * Endpoint to check application status.
     * Exposes GET /api/health
     *
     * @return simple status map
     */
    @GetMapping
    public Map<String, String> getHealth() {
        return Map.of("status", "UP");
    }
}
