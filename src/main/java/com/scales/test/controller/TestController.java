package com.scales.test.controller;

import com.scales.test.service.HeartbeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
public class TestController {

    @Autowired
    private HeartbeatService heartbeatService;

    @GetMapping("/api/v1/data")
    public ResponseEntity<Map<String, String>> getData() {
        Map<String, String> response = Map.of(
                "message", "Ping from backend server",
                "timestamp", Instant.now().toString(),
                "instance_id", System.getenv("HOSTNAME") != null ? System.getenv("HOSTNAME") : "localhost"
        );

        return ResponseEntity.ok()
                .header("Content-Type", "application/json")
                .body(response);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> response = Map.of(
                "status", "healthy"
        );

        return ResponseEntity.ok()
                .header("Content-Type", "application/json")
                .body(response);
    }

    @Scheduled(fixedRateString = "${config.scheduler.fixed-rate}", initialDelayString = "${config.scheduler.initial-delay}")
    public void scheduledPublishToHeartbeatQueue() {
        heartbeatService.sendHeartbeat();
    }
}
