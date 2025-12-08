package com.example.alina.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/api")
    public String apiRoot() {
        return "API is running";
    }

    @GetMapping("/api/health")
    public String health() {
        return "OK";
    }
}
