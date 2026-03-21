package com.sunking.payg.controller;

import com.sunking.payg.dto.PaymentDTO;
import com.sunking.payg.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payment Management", description = "APIs for processing and managing payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @Operation(summary = "Process a payment", description = "Process a payment for a device assignment. Automatically unlocks device if locked.")
    public ResponseEntity<PaymentDTO.Response> processPayment(@Valid @RequestBody PaymentDTO.CreateRequest request) {
        PaymentDTO.Response response = paymentService.processPayment(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get payment history", description = "Get payment history for a specific customer")
    public ResponseEntity<PaymentDTO.PaymentHistory> getPaymentHistory(
            @PathVariable Long customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "paymentDate"));
        PaymentDTO.PaymentHistory history = paymentService.getPaymentHistory(customerId, pageable);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get payment by ID", description = "Retrieve payment details by payment ID")
    public ResponseEntity<PaymentDTO.Response> getPaymentById(@PathVariable Long id) {
        PaymentDTO.Response response = paymentService.getPaymentById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all payments", description = "Retrieve all payments with pagination")
    public ResponseEntity<Page<PaymentDTO.Response>> getAllPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "paymentDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<PaymentDTO.Response> payments = paymentService.getAllPayments(pageable);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/assignment/{assignmentId}")
    @Operation(summary = "Get payments by assignment", description = "Get all payments for a specific device assignment")
    public ResponseEntity<Page<PaymentDTO.Response>> getPaymentsByAssignment(
            @PathVariable Long assignmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "paymentDate"));
        Page<PaymentDTO.Response> payments = paymentService.getPaymentsByAssignment(assignmentId, pageable);
        return ResponseEntity.ok(payments);
    }

    @PostMapping("/{id}/retry")
    @Operation(summary = "Retry failed payment", description = "Retry a failed payment transaction")
    public ResponseEntity<PaymentDTO.Response> retryPayment(@PathVariable Long id) {
        PaymentDTO.Response response = paymentService.retryPayment(id);
        return ResponseEntity.ok(response);
    }
}
