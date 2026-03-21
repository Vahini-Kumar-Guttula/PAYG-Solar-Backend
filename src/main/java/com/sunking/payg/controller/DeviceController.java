package com.sunking.payg.controller;

import com.sunking.payg.dto.DeviceDTO;
import com.sunking.payg.service.DeviceService;
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
@RequestMapping("/api/v1/devices")
@RequiredArgsConstructor
@Tag(name = "Device Management", description = "APIs for managing solar devices")
public class DeviceController {

    private final DeviceService deviceService;

    @PostMapping
    @Operation(summary = "Register a new device", description = "Register a new solar device in the inventory")
    public ResponseEntity<DeviceDTO.Response> registerDevice(@Valid @RequestBody DeviceDTO.CreateRequest request) {
        DeviceDTO.Response response = deviceService.registerDevice(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/{id}/assign")
    @Operation(summary = "Assign device to customer", description = "Assign a solar device to a customer with payment plan")
    public ResponseEntity<DeviceDTO.Response> assignDevice(
            @PathVariable Long id,
            @Valid @RequestBody DeviceDTO.AssignRequest request) {
        DeviceDTO.Response response = deviceService.assignDevice(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/status")
    @Operation(summary = "Get device status", description = "Get detailed status of a device including assignment and payment information")
    public ResponseEntity<DeviceDTO.StatusResponse> getDeviceStatus(@PathVariable Long id) {
        DeviceDTO.StatusResponse response = deviceService.getDeviceStatus(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all devices", description = "Retrieve all devices with pagination")
    public ResponseEntity<Page<DeviceDTO.Response>> getAllDevices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {
        
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<DeviceDTO.Response> devices = deviceService.getAllDevices(pageable);
        return ResponseEntity.ok(devices);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get device by ID", description = "Retrieve device details by device ID")
    public ResponseEntity<DeviceDTO.StatusResponse> getDeviceById(@PathVariable Long id) {
        DeviceDTO.StatusResponse response = deviceService.getDeviceStatus(id);
        return ResponseEntity.ok(response);
    }
}
