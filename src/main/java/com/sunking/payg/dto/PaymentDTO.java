package com.sunking.payg.dto;

import com.sunking.payg.model.Payment;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        @NotNull(message = "Customer ID is required")
        private Long customerId;

        @NotNull(message = "Assignment ID is required")
        private Long assignmentId;

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        private BigDecimal amount;

        @NotNull(message = "Payment method is required")
        private Payment.PaymentMethod paymentMethod;

        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private Long customerId;
        private String customerName;
        private Long assignmentId;
        private BigDecimal amount;
        private Payment.PaymentMethod paymentMethod;
        private Payment.PaymentStatus status;
        private LocalDateTime paymentDate;
        private String externalReference;
        private String description;
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentHistory {
        private Long customerId;
        private String customerName;
        private BigDecimal totalPaid;
        private Integer totalPayments;
        private java.util.List<Response> payments;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GatewayRequest {
        private String customerId;
        private String phoneNumber;
        private BigDecimal amount;
        private String currency;
        private String reference;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GatewayResponse {
        private boolean success;
        private String transactionId;
        private String status;
        private String message;
    }
}
