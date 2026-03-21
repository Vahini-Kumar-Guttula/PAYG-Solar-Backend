package com.sunking.payg.service;

import com.sunking.payg.dto.DeviceDTO;
import com.sunking.payg.exception.ResourceAlreadyExistsException;
import com.sunking.payg.exception.ResourceNotFoundException;
import com.sunking.payg.exception.BusinessException;
import com.sunking.payg.model.Customer;
import com.sunking.payg.model.Device;
import com.sunking.payg.model.DeviceAssignment;
import com.sunking.payg.repository.CustomerRepository;
import com.sunking.payg.repository.DeviceAssignmentRepository;
import com.sunking.payg.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final DeviceAssignmentRepository assignmentRepository;
    private final CustomerRepository customerRepository;

    @Value("${app.payment.overdue-days}")
    private int overdueDays;

    @Transactional
    public DeviceDTO.Response registerDevice(DeviceDTO.CreateRequest request) {
        log.info("Registering device with serial number: {}", request.getSerialNumber());

        if (deviceRepository.existsBySerialNumber(request.getSerialNumber())) {
            throw new ResourceAlreadyExistsException("Device with serial number " + request.getSerialNumber() + " already exists");
        }

        Device device = Device.builder()
                .serialNumber(request.getSerialNumber())
                .model(request.getModel())
                .manufacturer(request.getManufacturer())
                .totalCost(request.getTotalCost())
                .status(Device.DeviceStatus.INACTIVE)
                .description(request.getDescription())
                .build();

        Device savedDevice = deviceRepository.save(device);
        log.info("Device registered successfully with ID: {}", savedDevice.getId());

        return mapToResponse(savedDevice);
    }

    @Transactional
    public DeviceDTO.Response assignDevice(Long deviceId, DeviceDTO.AssignRequest request) {
        log.info("Assigning device {} to customer {}", deviceId, request.getCustomerId());

        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with ID: " + deviceId));

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + request.getCustomerId()));

        // Check if device is already assigned
        if (assignmentRepository.existsByDeviceIdAndStatus(deviceId, DeviceAssignment.AssignmentStatus.ACTIVE)) {
            throw new BusinessException("Device is already assigned to another customer");
        }

        // Create assignment
        DeviceAssignment assignment = DeviceAssignment.builder()
                .customer(customer)
                .device(device)
                .totalCost(device.getTotalCost())
                .amountPaid(BigDecimal.ZERO)
                .remainingBalance(device.getTotalCost())
                .paymentPlan(DeviceAssignment.PaymentPlan.valueOf(request.getPaymentPlan()))
                .installmentAmount(request.getInstallmentAmount())
                .assignmentDate(LocalDate.now())
                .nextPaymentDueDate(calculateNextPaymentDate(request.getPaymentPlan()))
                .missedPayments(0)
                .status(DeviceAssignment.AssignmentStatus.ACTIVE)
                .build();

        assignmentRepository.save(assignment);

        // Update device status to ACTIVE
        device.setStatus(Device.DeviceStatus.ACTIVE);
        deviceRepository.save(device);

        log.info("Device assigned successfully. Assignment ID: {}", assignment.getId());
        return mapToResponse(device);
    }

    @Transactional(readOnly = true)
    public DeviceDTO.StatusResponse getDeviceStatus(Long deviceId) {
        log.info("Fetching device status for ID: {}", deviceId);

        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with ID: " + deviceId));

        DeviceAssignment assignment = assignmentRepository.findByDeviceId(deviceId).orElse(null);

        DeviceDTO.StatusResponse.StatusResponseBuilder builder = DeviceDTO.StatusResponse.builder()
                .deviceId(device.getId())
                .serialNumber(device.getSerialNumber())
                .status(device.getStatus())
                .totalCost(device.getTotalCost());

        if (assignment != null) {
            builder.assignmentId(assignment.getId())
                    .customerId(assignment.getCustomer().getId())
                    .customerName(assignment.getCustomer().getFirstName() + " " + assignment.getCustomer().getLastName())
                    .amountPaid(assignment.getAmountPaid())
                    .remainingBalance(assignment.getRemainingBalance())
                    .missedPayments(assignment.getMissedPayments());
        }

        return builder.build();
    }

    @Transactional(readOnly = true)
    public Page<DeviceDTO.Response> getAllDevices(Pageable pageable) {
        log.info("Fetching all devices with pagination: {}", pageable);
        return deviceRepository.findAll(pageable).map(this::mapToResponse);
    }

    /**
     * CRITICAL BUSINESS LOGIC: Device Locking
     * Scheduled job that runs daily to check for overdue payments and lock devices
     */
    @Scheduled(cron = "0 0 2 * * *") // Runs at 2 AM daily
    @Transactional
    public void checkAndLockOverdueDevices() {
        log.info("Starting scheduled job to check and lock overdue devices");

        LocalDate cutoffDate = LocalDate.now().minusDays(overdueDays);
        List<DeviceAssignment> overdueAssignments = assignmentRepository.findOverdueAssignments(cutoffDate);

        log.info("Found {} overdue assignments", overdueAssignments.size());

        for (DeviceAssignment assignment : overdueAssignments) {
            lockDevice(assignment);
        }

        log.info("Completed checking and locking overdue devices");
    }

    /**
     * Lock a device due to overdue payment
     */
    @Transactional
    public void lockDevice(DeviceAssignment assignment) {
        Device device = assignment.getDevice();
        
        if (device.getStatus() != Device.DeviceStatus.LOCKED) {
            device.setStatus(Device.DeviceStatus.LOCKED);
            deviceRepository.save(device);
            
            log.warn("Device {} locked due to overdue payment. Customer: {}, Missed payments: {}", 
                    device.getSerialNumber(), 
                    assignment.getCustomer().getEmail(),
                    assignment.getMissedPayments());
        }
    }

    /**
     * Unlock a device after payment is made
     */
    @Transactional
    public void unlockDevice(DeviceAssignment assignment) {
        Device device = assignment.getDevice();
        
        if (device.getStatus() == Device.DeviceStatus.LOCKED) {
            device.setStatus(Device.DeviceStatus.ACTIVE);
            deviceRepository.save(device);
            
            log.info("Device {} unlocked after payment. Customer: {}", 
                    device.getSerialNumber(), 
                    assignment.getCustomer().getEmail());
        }
    }

    private LocalDate calculateNextPaymentDate(String paymentPlan) {
        LocalDate today = LocalDate.now();
        return switch (DeviceAssignment.PaymentPlan.valueOf(paymentPlan)) {
            case DAILY -> today.plusDays(1);
            case WEEKLY -> today.plusWeeks(1);
            case MONTHLY -> today.plusMonths(1);
        };
    }

    private DeviceDTO.Response mapToResponse(Device device) {
        return DeviceDTO.Response.builder()
                .id(device.getId())
                .serialNumber(device.getSerialNumber())
                .model(device.getModel())
                .manufacturer(device.getManufacturer())
                .totalCost(device.getTotalCost())
                .status(device.getStatus())
                .description(device.getDescription())
                .createdAt(device.getCreatedAt())
                .updatedAt(device.getUpdatedAt())
                .build();
    }
}
