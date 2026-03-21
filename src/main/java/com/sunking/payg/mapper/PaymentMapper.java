package com.sunking.payg.mapper;

import com.sunking.payg.dto.PaymentDTO;
import com.sunking.payg.model.Customer;
import com.sunking.payg.model.DeviceAssignment;
import com.sunking.payg.model.Payment;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class PaymentMapper {

    public Payment toEntity(PaymentDTO.CreateRequest request, Customer customer, DeviceAssignment assignment) {
        return Payment.builder()
                .customer(customer)
                .assignment(assignment)
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .status(Payment.PaymentStatus.PENDING)
                .paymentDate(LocalDateTime.now())
                .description(request.getDescription())
                .build();
    }

    public PaymentDTO.Response toResponse(Payment payment) {
        return PaymentDTO.Response.builder()
                .id(payment.getId())
                .customerId(payment.getCustomer().getId())
                .customerName(payment.getCustomer().getFirstName() + " " + payment.getCustomer().getLastName())
                .assignmentId(payment.getAssignment().getId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getStatus())
                .paymentDate(payment.getPaymentDate())
                .externalReference(payment.getExternalReference())
                .description(payment.getDescription())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
