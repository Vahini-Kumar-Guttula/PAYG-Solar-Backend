package com.sunking.payg.dto;

import com.sunking.payg.model.Device;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class DeviceDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        @NotBlank(message = "Serial number is required")
        private String serialNumber;

        @NotBlank(message = "Model is required")
        private String model;

        @NotBlank(message = "Manufacturer is required")
        private String manufacturer;

        @NotNull(message = "Total cost is required")
        @DecimalMin(value = "0.01", message = "Total cost must be greater than 0")
        private BigDecimal totalCost;

        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String serialNumber;
        private String model;
        private String manufacturer;
        private BigDecimal totalCost;
        private Device.DeviceStatus status;
        private String description;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssignRequest {
        @NotNull(message = "Customer ID is required")
        private Long customerId;

        @NotNull(message = "Payment plan is required")
        private String paymentPlan; // DAILY, WEEKLY, MONTHLY

        @NotNull(message = "Installment amount is required")
        @DecimalMin(value = "0.01", message = "Installment amount must be greater than 0")
        private BigDecimal installmentAmount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusResponse {
        private Long deviceId;
        private String serialNumber;
        private Device.DeviceStatus status;
        private Long assignmentId;
        private Long customerId;
        private String customerName;
        private BigDecimal totalCost;
        private BigDecimal amountPaid;
        private BigDecimal remainingBalance;
        private Integer missedPayments;
    }
}
