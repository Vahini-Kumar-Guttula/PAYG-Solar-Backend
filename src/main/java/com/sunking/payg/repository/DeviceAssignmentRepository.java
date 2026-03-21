package com.sunking.payg.repository;

import com.sunking.payg.model.DeviceAssignment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceAssignmentRepository extends JpaRepository<DeviceAssignment, Long> {

    @Query("SELECT da FROM DeviceAssignment da " +
           "JOIN FETCH da.customer " +
           "JOIN FETCH da.device " +
           "WHERE da.customer.id = :customerId")
    Page<DeviceAssignment> findByCustomerId(@Param("customerId") Long customerId, Pageable pageable);

    @Query("SELECT da FROM DeviceAssignment da " +
           "JOIN FETCH da.customer " +
           "JOIN FETCH da.device " +
           "WHERE da.device.id = :deviceId")
    Optional<DeviceAssignment> findByDeviceId(@Param("deviceId") Long deviceId);

    @Query("SELECT da FROM DeviceAssignment da " +
           "JOIN FETCH da.customer " +
           "JOIN FETCH da.device " +
           "WHERE da.status = :status")
    Page<DeviceAssignment> findByStatus(@Param("status") DeviceAssignment.AssignmentStatus status, Pageable pageable);

    @Query("SELECT da FROM DeviceAssignment da " +
           "WHERE da.nextPaymentDueDate < :date " +
           "AND da.status = 'ACTIVE'")
    List<DeviceAssignment> findOverdueAssignments(@Param("date") LocalDate date);

    @Query("SELECT da FROM DeviceAssignment da " +
           "WHERE da.customer.id = :customerId " +
           "AND da.device.id = :deviceId " +
           "AND da.status = 'ACTIVE'")
    Optional<DeviceAssignment> findActiveAssignmentByCustomerAndDevice(
        @Param("customerId") Long customerId,
        @Param("deviceId") Long deviceId
    );

    boolean existsByDeviceIdAndStatus(Long deviceId, DeviceAssignment.AssignmentStatus status);
}
