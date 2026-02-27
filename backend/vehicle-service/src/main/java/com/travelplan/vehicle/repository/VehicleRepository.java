package com.travelplan.vehicle.repository;

import com.travelplan.vehicle.entity.Vehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

        @Query("SELECT v FROM Vehicle v WHERE v.isActive = true " +
                        "AND (:vehicleType IS NULL OR v.vehicleType = :vehicleType) " +
                        "AND (:minCapacity IS NULL OR v.seatingCapacity >= :minCapacity) " +
                        "AND (:minDailyRate IS NULL OR v.dailyRate >= :minDailyRate) " +
                        "AND (:maxDailyRate IS NULL OR v.dailyRate <= :maxDailyRate) " +
                        "AND (:query IS NULL OR LOWER(v.make) LIKE LOWER(CONCAT('%', CAST(:query AS string), '%')) " +
                        "OR LOWER(v.model) LIKE LOWER(CONCAT('%', CAST(:query AS string), '%')))")
        Page<Vehicle> searchVehicles(
                        @Param("vehicleType") String vehicleType,
                        @Param("minCapacity") Integer minCapacity,
                        @Param("minDailyRate") BigDecimal minDailyRate,
                        @Param("maxDailyRate") BigDecimal maxDailyRate,
                        @Param("query") String query,
                        Pageable pageable);

        List<Vehicle> findByOwnerIdAndIsActiveTrue(String ownerId);

        List<Vehicle> findByIsActiveTrue();

        // NEW: Checks if a license plate already exists in the database
        boolean existsByLicensePlate(String licensePlate);
}