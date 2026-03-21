package com.sunking.payg.service;

import com.sunking.payg.dto.PaymentDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.UUID;

@Service
@Slf4j
public class PaymentGatewayService {

    private final WebClient webClient;
    private final String gatewayUrl;
    private final String apiKey;
    private final int timeout;

    public PaymentGatewayService(
            WebClient.Builder webClientBuilder,
            @Value("${app.payment.gateway.url}") String gatewayUrl,
            @Value("${app.payment.gateway.api-key}") String apiKey,
            @Value("${app.payment.gateway.timeout}") int timeout) {
        this.gatewayUrl = gatewayUrl;
        this.apiKey = apiKey;
        this.timeout = timeout;
        this.webClient = webClientBuilder.baseUrl(gatewayUrl).build();
    }

    /**
     * Process payment through external Mobile Money gateway
     * This is a mock implementation that simulates external API call
     */
    public PaymentDTO.GatewayResponse processPayment(PaymentDTO.GatewayRequest request) {
        log.info("Processing payment through gateway for customer: {}, amount: {}", 
                request.getCustomerId(), request.getAmount());

        try {
            // In production, this would make actual HTTP call to payment gateway
            // For now, we simulate the call with mock response
            return simulateMockGatewayCall(request);
            
            // Real implementation would look like:
            /*
            return webClient.post()
                    .uri("/payments")
                    .header("Authorization", "Bearer " + apiKey)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(PaymentDTO.GatewayResponse.class)
                    .timeout(Duration.ofMillis(timeout))
                    .block();
            */
        } catch (Exception e) {
            log.error("Payment gateway error: {}", e.getMessage(), e);
            return PaymentDTO.GatewayResponse.builder()
                    .success(false)
                    .status("FAILED")
                    .message("Payment gateway error: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Mock implementation simulating external payment gateway
     * In production, this would be replaced with actual API call
     */
    private PaymentDTO.GatewayResponse simulateMockGatewayCall(PaymentDTO.GatewayRequest request) {
        // Simulate network delay
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Simulate 95% success rate
        boolean success = Math.random() < 0.95;

        if (success) {
            log.info("Mock gateway: Payment successful for reference: {}", request.getReference());
            return PaymentDTO.GatewayResponse.builder()
                    .success(true)
                    .transactionId(UUID.randomUUID().toString())
                    .status("COMPLETED")
                    .message("Payment processed successfully")
                    .build();
        } else {
            log.warn("Mock gateway: Payment failed for reference: {}", request.getReference());
            return PaymentDTO.GatewayResponse.builder()
                    .success(false)
                    .status("FAILED")
                    .message("Insufficient funds or network error")
                    .build();
        }
    }

    /**
     * Verify payment status with gateway
     */
    public PaymentDTO.GatewayResponse verifyPayment(String transactionId) {
        log.info("Verifying payment with transaction ID: {}", transactionId);
        
        // Mock verification - always returns success
        return PaymentDTO.GatewayResponse.builder()
                .success(true)
                .transactionId(transactionId)
                .status("COMPLETED")
                .message("Payment verified")
                .build();
    }
}
