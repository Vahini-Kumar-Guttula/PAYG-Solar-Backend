package com.sunking.payg.config;

import com.sunking.payg.model.Customer;
import com.sunking.payg.model.Device;
import com.sunking.payg.model.DeviceAssignment;
import com.sunking.payg.model.Payment;
import com.sunking.payg.repository.CustomerRepository;
import com.sunking.payg.repository.DeviceAssignmentRepository;
import com.sunking.payg.repository.DeviceRepository;
import com.sunking.payg.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Data Initialization Configuration
 * Loads sample data for development and testing
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    @Bean
    @Profile("!mysql") // Run only when NOT using MySQL profile
    CommandLineRunner initDatabase(
            CustomerRepository customerRepository,
            DeviceRepository deviceRepository,
            DeviceAssignmentRepository assignmentRepository,
            PaymentRepository paymentRepository) {
        
        return args -> {
            log.info("Initializing sample data...");
            
            // Create Customers
            Customer customer1 = Customer.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .email("john.doe@example.com")
                    .phoneNumber("+254712345678")
                    .address("123 Main St")
                    .city("Nairobi")
                    .country("Kenya")
                    .status(Customer.CustomerStatus.ACTIVE)
                    .build();
            customer1 = customerRepository.save(customer1);
            
            Customer customer2 = Customer.builder()
                    .firstName("Jane")
                    .lastName("Smith")
                    .email("jane.smith@example.com")
                    .phoneNumber("+254723456789")
                    .address("456 Oak Ave")
                    .city("Mombasa")
                    .country("Kenya")
                    .status(Customer.CustomerStatus.ACTIVE)
                    .build();
            customer2 = customerRepository.save(customer2);
            
            Customer customer3 = Customer.builder()
                    .firstName("Bob")
                    .lastName("Johnson")
                    .email("bob.johnson@example.com")
                    .phoneNumber("+254734567890")
                    .address("789 Pine Rd")
                    .city("Kisumu")
                    .country("Kenya")
                    .status(Customer.CustomerStatus.ACTIVE)
                    .build();
            customer3 = customerRepository.save(customer3);
            
            log.info("Created {} customers", customerRepository.count());
            
            // Create Devices
            Device device1 = Device.builder()
                    .serialNumber("SN-2024-001")
                    .model("SolarMax 100W")
                    .manufacturer("SunKing")
                    .totalCost(new BigDecimal("50000.00"))
                    .description("100W Solar Panel with battery and LED lights")
                    .status(Device.DeviceStatus.INACTIVE)
                    .build();
            device1 = deviceRepository.save(device1);
            
            Device device2 = Device.builder()
                    .serialNumber("SN-2024-002")
                    .model("SolarMax 200W")
                    .manufacturer("SunKing")
                    .totalCost(new BigDecimal("75000.00"))
                    .description("200W Solar Panel with battery and LED lights")
                    .status(Device.DeviceStatus.INACTIVE)
                    .build();
            device2 = deviceRepository.save(device2);
            
            Device device3 = Device.builder()
                    .serialNumber("SN-2024-003")
                    .model("SolarMax 300W")
                    .manufacturer("SunKing")
                    .totalCost(new BigDecimal("100000.00"))
                    .description("300W Solar Panel with battery, LED lights and TV")
                    .status(Device.DeviceStatus.INACTIVE)
                    .build();
            device3 = deviceRepository.save(device3);
            
            Device device4 = Device.builder()
                    .serialNumber("SN-2024-004")
                    .model("SolarMax 100W")
                    .manufacturer("SunKing")
                    .totalCost(new BigDecimal("50000.00"))
                    .description("100W Solar Panel with battery and LED lights")
                    .status(Device.DeviceStatus.INACTIVE)
                    .build();
            device4 = deviceRepository.save(device4);
            
            Device device5 = Device.builder()
                    .serialNumber("SN-2024-005")
                    .model("SolarMax 200W")
                    .manufacturer("SunKing")
                    .totalCost(new BigDecimal("75000.00"))
                    .description("200W Solar Panel with battery and LED lights")
                    .status(Device.DeviceStatus.INACTIVE)
                    .build();
            device5 = deviceRepository.save(device5);
            
            log.info("Created {} devices", deviceRepository.count());
            
            // Create Device Assignments
            DeviceAssignment assignment1 = DeviceAssignment.builder()
                    .customer(customer1)
                    .device(device1)
                    .totalCost(new BigDecimal("50000.00"))
                    .amountPaid(new BigDecimal("5000.00"))
                    .remainingBalance(new BigDecimal("45000.00"))
                    .paymentPlan(DeviceAssignment.PaymentPlan.WEEKLY)
                    .installmentAmount(new BigDecimal("500.00"))
                    .assignmentDate(LocalDate.now())
                    .lastPaymentDate(LocalDate.now())
                    .nextPaymentDueDate(LocalDate.now().plusDays(7))
                    .missedPayments(0)
                    .status(DeviceAssignment.AssignmentStatus.ACTIVE)
                    .build();
            assignment1 = assignmentRepository.save(assignment1);
            
            // Update device1 status to ACTIVE
            device1.setStatus(Device.DeviceStatus.ACTIVE);
            deviceRepository.save(device1);
            
            DeviceAssignment assignment2 = DeviceAssignment.builder()
                    .customer(customer2)
                    .device(device2)
                    .totalCost(new BigDecimal("75000.00"))
                    .amountPaid(new BigDecimal("7500.00"))
                    .remainingBalance(new BigDecimal("67500.00"))
                    .paymentPlan(DeviceAssignment.PaymentPlan.WEEKLY)
                    .installmentAmount(new BigDecimal("750.00"))
                    .assignmentDate(LocalDate.now())
                    .lastPaymentDate(LocalDate.now())
                    .nextPaymentDueDate(LocalDate.now().plusDays(7))
                    .missedPayments(0)
                    .status(DeviceAssignment.AssignmentStatus.ACTIVE)
                    .build();
            assignment2 = assignmentRepository.save(assignment2);
            
            // Update device2 status to ACTIVE
            device2.setStatus(Device.DeviceStatus.ACTIVE);
            deviceRepository.save(device2);
            
            DeviceAssignment assignment3 = DeviceAssignment.builder()
                    .customer(customer3)
                    .device(device3)
                    .totalCost(new BigDecimal("100000.00"))
                    .amountPaid(new BigDecimal("0.00"))
                    .remainingBalance(new BigDecimal("100000.00"))
                    .paymentPlan(DeviceAssignment.PaymentPlan.MONTHLY)
                    .installmentAmount(new BigDecimal("5000.00"))
                    .assignmentDate(LocalDate.now())
                    .lastPaymentDate(null)
                    .nextPaymentDueDate(LocalDate.now().plusDays(30))
                    .missedPayments(0)
                    .status(DeviceAssignment.AssignmentStatus.ACTIVE)
                    .build();
            assignment3 = assignmentRepository.save(assignment3);
            
            // Update device3 status to ACTIVE
            device3.setStatus(Device.DeviceStatus.ACTIVE);
            deviceRepository.save(device3);
            
            log.info("Created {} device assignments", assignmentRepository.count());
            
            // Create Payments
            Payment payment1 = Payment.builder()
                    .customer(customer1)
                    .assignment(assignment1)
                    .amount(new BigDecimal("500.00"))
                    .paymentMethod(Payment.PaymentMethod.MOBILE_MONEY)
                    .status(Payment.PaymentStatus.COMPLETED)
                    .paymentDate(LocalDateTime.now())
                    .externalReference("TXN-2024-001")
                    .description("Weekly payment")
                    .gatewayResponse("Payment successful")
                    .build();
            paymentRepository.save(payment1);
            
            Payment payment2 = Payment.builder()
                    .customer(customer1)
                    .assignment(assignment1)
                    .amount(new BigDecimal("500.00"))
                    .paymentMethod(Payment.PaymentMethod.MOBILE_MONEY)
                    .status(Payment.PaymentStatus.COMPLETED)
                    .paymentDate(LocalDateTime.now().minusDays(7))
                    .externalReference("TXN-2024-002")
                    .description("Weekly payment")
                    .gatewayResponse("Payment successful")
                    .build();
            paymentRepository.save(payment2);
            
            Payment payment3 = Payment.builder()
                    .customer(customer2)
                    .assignment(assignment2)
                    .amount(new BigDecimal("750.00"))
                    .paymentMethod(Payment.PaymentMethod.MOBILE_MONEY)
                    .status(Payment.PaymentStatus.COMPLETED)
                    .paymentDate(LocalDateTime.now())
                    .externalReference("TXN-2024-003")
                    .description("Weekly payment")
                    .gatewayResponse("Payment successful")
                    .build();
            paymentRepository.save(payment3);
            
            Payment payment4 = Payment.builder()
                    .customer(customer2)
                    .assignment(assignment2)
                    .amount(new BigDecimal("750.00"))
                    .paymentMethod(Payment.PaymentMethod.MOBILE_MONEY)
                    .status(Payment.PaymentStatus.COMPLETED)
                    .paymentDate(LocalDateTime.now().minusDays(7))
                    .externalReference("TXN-2024-004")
                    .description("Weekly payment")
                    .gatewayResponse("Payment successful")
                    .build();
            paymentRepository.save(payment4);
            
            log.info("Created {} payments", paymentRepository.count());
            
            log.info("Sample data initialization completed successfully!");
            log.info("Total: {} customers, {} devices, {} assignments, {} payments",
                    customerRepository.count(),
                    deviceRepository.count(),
                    assignmentRepository.count(),
                    paymentRepository.count());
        };
    }
}
