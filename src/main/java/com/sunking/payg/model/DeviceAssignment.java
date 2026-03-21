package com.sunking.payg.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "device_assignments", indexes = {
    @Index(name = "idx_assignment_customer", columnList = "customer_id"),
    @Index(name = "idx_assignment_device", columnList = "device_id"),
    @Index(name = "idx_assignment_status", columnList = "status"),
    @Index(name = "idx_assignment_next_payment", columnList = "nextPaymentDueDate")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalCost;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amountPaid;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal remainingBalance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentPlan paymentPlan;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal installmentAmount;

    @Column(nullable = false)
    private LocalDate assignmentDate;

    @Column
    private LocalDate nextPaymentDueDate;

    @Column
    private LocalDate lastPaymentDate;

    @Column(nullable = false)
    private Integer missedPayments;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AssignmentStatus status;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public enum PaymentPlan {
        DAILY,
        WEEKLY,
        MONTHLY
    }

    public enum AssignmentStatus {
        ACTIVE,
        COMPLETED,
        DEFAULTED,
        CANCELLED
    }

    @PrePersist
    public void prePersist() {
        if (amountPaid == null) {
            amountPaid = BigDecimal.ZERO;
        }
        if (remainingBalance == null) {
            remainingBalance = totalCost;
        }
        if (missedPayments == null) {
            missedPayments = 0;
        }
    }
}
