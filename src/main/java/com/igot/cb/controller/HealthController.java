package com.igot.cb.controller;

import com.igot.cb.model.SBApiResponse;
import com.igot.cb.service.HealthCheckService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    private final HealthCheckService healthCheckService;

    public HealthController(HealthCheckService healthCheckService) {
        this.healthCheckService = healthCheckService;
    }

    @GetMapping("/health")
    public ResponseEntity<SBApiResponse> health() {
        SBApiResponse response = healthCheckService.getHealthCheck();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}