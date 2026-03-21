package com.sunking.payg.repository;

import com.sunking.payg.model.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Query("SELECT p FROM Payment p " +
           "JOIN FETCH p.customer " +
           "JOIN FETCH p.assignment " +
           "WHERE p.customer.id = :customerId " +
           "ORDER BY p.paymentDate DESC")
    Page<Payment> findByCustomerId(@Param("customerId") Long customerId, Pageable pageable);

    @Query("SELECT p FROM Payment p " +
           "JOIN FETCH p.customer " +
           "JOIN FETCH p.assignment " +
           "WHERE p.assignment.id = :assignmentId " +
           "ORDER BY p.paymentDate DESC")
    Page<Payment> findByAssignmentId(@Param("assignmentId") Long assignmentId, Pageable pageable);

    @Query("SELECT p FROM Payment p WHERE p.status = :status")
    Page<Payment> findByStatus(@Param("status") Payment.PaymentStatus status, Pageable pageable);

    Optional<Payment> findByExternalReference(String externalReference);

    @Query("SELECT p FROM Payment p " +
           "WHERE p.customer.id = :customerId " +
           "AND p.paymentDate BETWEEN :startDate AND :endDate " +
           "ORDER BY p.paymentDate DESC")
    List<Payment> findByCustomerIdAndDateRange(
        @Param("customerId") Long customerId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT SUM(p.amount) FROM Payment p " +
           "WHERE p.customer.id = :customerId " +
           "AND p.status = 'COMPLETED'")
    Double getTotalPaymentsByCustomerId(@Param("customerId") Long customerId);

    @Query("SELECT SUM(p.amount) FROM Payment p " +
           "WHERE p.assignment.id = :assignmentId " +
           "AND p.status = 'COMPLETED'")
    Double getTotalPaymentsByAssignmentId(@Param("assignmentId") Long assignmentId);
}
