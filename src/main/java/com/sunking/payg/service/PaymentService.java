package com.sunking.payg.service;

import com.sunking.payg.dto.PaymentDTO;
import com.sunking.payg.exception.BusinessException;
import com.sunking.payg.exception.ResourceNotFoundException;
import com.sunking.payg.mapper.PaymentMapper;
import com.sunking.payg.model.Customer;
import com.sunking.payg.model.DeviceAssignment;
import com.sunking.payg.model.Payment;
import com.sunking.payg.repository.CustomerRepository;
import com.sunking.payg.repository.DeviceAssignmentRepository;
import com.sunking.payg.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final CustomerRepository customerRepository;
    private final DeviceAssignmentRepository assignmentRepository;
    private final PaymentMapper paymentMapper;
    private final PaymentGatewayService paymentGatewayService;
    private final DeviceService deviceService;

    /**
     * CRITICAL BUSINESS LOGIC: Process payment and handle device locking/unlocking
     */
    @Transactional
    public PaymentDTO.Response processPayment(PaymentDTO.CreateRequest request) {
        log.info("Processing payment for customer: {}, assignment: {}, amount: {}", 
                request.getCustomerId(), request.getAssignmentId(), request.getAmount());

        // Validate customer
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + request.getCustomerId()));

        // Validate assignment
        DeviceAssignment assignment = assignmentRepository.findById(request.getAssignmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Device assignment not found with ID: " + request.getAssignmentId()));

        // Verify assignment belongs to customer
        if (!assignment.getCustomer().getId().equals(customer.getId())) {
            throw new BusinessException("Assignment does not belong to the specified customer");
        }

        // Verify assignment is active
        if (assignment.getStatus() != DeviceAssignment.AssignmentStatus.ACTIVE) {
            throw new BusinessException("Cannot process payment for inactive assignment");
        }

        // Verify payment amount doesn't exceed remaining balance
        if (request.getAmount().compareTo(assignment.getRemainingBalance()) > 0) {
            throw new BusinessException("Payment amount exceeds remaining balance");
        }

        // Create payment record
        Payment payment = paymentMapper.toEntity(request, customer, assignment);
        String reference = generatePaymentReference();
        payment.setExternalReference(reference);

        // Call external payment gateway
        PaymentDTO.GatewayRequest gatewayRequest = PaymentDTO.GatewayRequest.builder()
                .customerId(customer.getId().toString())
                .phoneNumber(customer.getPhoneNumber())
                .amount(request.getAmount())
                .currency("USD")
                .reference(reference)
                .build();

        PaymentDTO.GatewayResponse gatewayResponse = paymentGatewayService.processPayment(gatewayRequest);

        // Update payment based on gateway response
        if (gatewayResponse.isSuccess()) {
            payment.setStatus(Payment.PaymentStatus.COMPLETED);
            payment.setGatewayResponse(gatewayResponse.getMessage());
            
            // Update assignment
            updateAssignmentAfterPayment(assignment, request.getAmount());
            
            // CRITICAL: Unlock device if it was locked
            deviceService.unlockDevice(assignment);
            
            log.info("Payment processed successfully. Payment ID: {}, Transaction ID: {}", 
                    payment.getId(), gatewayResponse.getTransactionId());
        } else {
            payment.setStatus(Payment.PaymentStatus.FAILED);
            payment.setGatewayResponse(gatewayResponse.getMessage());
            log.warn("Payment failed for customer: {}. Reason: {}", 
                    customer.getId(), gatewayResponse.getMessage());
        }

        Payment savedPayment = paymentRepository.save(payment);
        return paymentMapper.toResponse(savedPayment);
    }

    /**
     * Update device assignment after successful payment
     */
    private void updateAssignmentAfterPayment(DeviceAssignment assignment, BigDecimal paymentAmount) {
        // Update amounts
        BigDecimal newAmountPaid = assignment.getAmountPaid().add(paymentAmount);
        BigDecimal newRemainingBalance = assignment.getRemainingBalance().subtract(paymentAmount);
        
        assignment.setAmountPaid(newAmountPaid);
        assignment.setRemainingBalance(newRemainingBalance);
        assignment.setLastPaymentDate(LocalDate.now());
        
        // Calculate next payment due date
        LocalDate nextDueDate = calculateNextPaymentDate(assignment.getPaymentPlan());
        assignment.setNextPaymentDueDate(nextDueDate);
        
        // Reset missed payments counter
        assignment.setMissedPayments(0);
        
        // Check if fully paid
        if (newRemainingBalance.compareTo(BigDecimal.ZERO) <= 0) {
            assignment.setStatus(DeviceAssignment.AssignmentStatus.COMPLETED);
            assignment.setNextPaymentDueDate(null);
            log.info("Device assignment {} fully paid. Total paid: {}", 
                    assignment.getId(), newAmountPaid);
        }
        
        assignmentRepository.save(assignment);
    }

    /**
     * Get payment history for a customer
     */
    @Transactional(readOnly = true)
    public PaymentDTO.PaymentHistory getPaymentHistory(Long customerId, Pageable pageable) {
        log.info("Fetching payment history for customer: {}", customerId);
        
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + customerId));
        
        Page<Payment> paymentsPage = paymentRepository.findByCustomerId(customerId, pageable);
        List<PaymentDTO.Response> payments = paymentsPage.getContent().stream()
                .map(paymentMapper::toResponse)
                .toList();
        
        Double totalPaid = paymentRepository.getTotalPaymentsByCustomerId(customerId);
        
        return PaymentDTO.PaymentHistory.builder()
                .customerId(customerId)
                .customerName(customer.getFirstName() + " " + customer.getLastName())
                .totalPaid(totalPaid != null ? BigDecimal.valueOf(totalPaid) : BigDecimal.ZERO)
                .totalPayments(payments.size())
                .payments(payments)
                .build();
    }

    /**
     * Get all payments with pagination
     */
    @Transactional(readOnly = true)
    public Page<PaymentDTO.Response> getAllPayments(Pageable pageable) {
        log.info("Fetching all payments with pagination: {}", pageable);
        return paymentRepository.findAll(pageable)
                .map(paymentMapper::toResponse);
    }

    /**
     * Get payment by ID
     */
    @Transactional(readOnly = true)
    public PaymentDTO.Response getPaymentById(Long id) {
        log.info("Fetching payment with ID: {}", id);
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + id));
        return paymentMapper.toResponse(payment);
    }

    /**
     * Get payments by assignment
     */
    @Transactional(readOnly = true)
    public Page<PaymentDTO.Response> getPaymentsByAssignment(Long assignmentId, Pageable pageable) {
        log.info("Fetching payments for assignment: {}", assignmentId);
        return paymentRepository.findByAssignmentId(assignmentId, pageable)
                .map(paymentMapper::toResponse);
    }

    /**
     * Retry failed payment
     */
    @Transactional
    public PaymentDTO.Response retryPayment(Long paymentId) {
        log.info("Retrying payment with ID: {}", paymentId);
        
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + paymentId));
        
        if (payment.getStatus() != Payment.PaymentStatus.FAILED) {
            throw new BusinessException("Can only retry failed payments");
        }
        
        // Create new payment request
        PaymentDTO.CreateRequest retryRequest = PaymentDTO.CreateRequest.builder()
                .customerId(payment.getCustomer().getId())
                .assignmentId(payment.getAssignment().getId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .description("Retry of payment #" + paymentId)
                .build();
        
        return processPayment(retryRequest);
    }

    private LocalDate calculateNextPaymentDate(DeviceAssignment.PaymentPlan paymentPlan) {
        LocalDate today = LocalDate.now();
        return switch (paymentPlan) {
            case DAILY -> today.plusDays(1);
            case WEEKLY -> today.plusWeeks(1);
            case MONTHLY -> today.plusMonths(1);
        };
    }

    private String generatePaymentReference() {
        return "PAY-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
