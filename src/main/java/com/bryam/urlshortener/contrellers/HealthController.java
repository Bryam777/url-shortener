package com.bryam.urlshortener.contrellers;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/health")
public class HealthController {

    // Endpoint para monitorear y verificar que el servidor esta activo
    public ResponseEntity<Map<String, Object>> health() {

        log.debug("Health check requested");

        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        response.put("service", "URL Shortener");
        response.put("version", "1.0.0");

        return ResponseEntity.ok(response);
    }

    // Endpoint para verificar conexión con la BD
    public ResponseEntity<Map<String, Object>> healthDatabase() {

        Map<String, Object> response = new HashMap<>();

        try {
            response.put("database", "up");
            response.put("status", "Connected");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Database health check error", e);
            response.put("database", "DOWN");
            response.put("error", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
}
