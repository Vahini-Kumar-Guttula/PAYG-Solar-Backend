package com.sunking.payg.repository;

import com.sunking.payg.model.Device;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {

    Optional<Device> findBySerialNumber(String serialNumber);

    boolean existsBySerialNumber(String serialNumber);

    @Query("SELECT d FROM Device d WHERE d.status = :status")
    Page<Device> findByStatus(Device.DeviceStatus status, Pageable pageable);

    @Query("SELECT d FROM Device d WHERE " +
           "LOWER(d.serialNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(d.model) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(d.manufacturer) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Device> searchDevices(String searchTerm, Pageable pageable);
}
