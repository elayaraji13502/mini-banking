package com.banfico.minibanking.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of(
                "status", "UP",
                "service", "Mini Banking Backend"
        );
    }

    @GetMapping("/api/info")
    public Map<String, String> info() {
        return Map.of(
                "application", "Banfico Mini Banking",
                "version", "1.0.0",
                "environment", "Development"
        );
    }
}