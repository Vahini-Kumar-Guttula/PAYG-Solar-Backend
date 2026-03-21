package com.sunking.payg.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Root API Controller
 * Provides API information and available endpoints
 */
@RestController
@RequestMapping("/api/v1")
@Hidden
public class RootController {

    @GetMapping
    public ResponseEntity<Map<String, Object>> getApiInfo() {
        Map<String, Object> apiInfo = new HashMap<>();
        apiInfo.put("name", "PAYG Solar System Backend API");
        apiInfo.put("version", "1.0.0");
        apiInfo.put("description", "Pay-As-You-Go Solar System Management API");
        
        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("customers", "/api/v1/customers");
        endpoints.put("devices", "/api/v1/devices");
        endpoints.put("payments", "/api/v1/payments");
        endpoints.put("swagger-ui", "/swagger-ui.html");
        endpoints.put("api-docs", "/api-docs");
        endpoints.put("health", "/actuator/health");
        
        apiInfo.put("endpoints", endpoints);
        apiInfo.put("status", "running");
        
        return ResponseEntity.ok(apiInfo);
    }
}
