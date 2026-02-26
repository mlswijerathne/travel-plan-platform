package com.travelplan.vehicle.repository;

import com.travelplan.vehicle.entity.Booking;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingRepository extends JpaRepository<Booking, Long> {
        // Find all bookings for a specific customer
        List<Booking> findByCustomerEmail(String email);

        @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.vehicleId = :vehicleId " +
                        "AND b.status = :status " +
                        "AND b.startDate <= :endDate " +
                        "AND b.endDate >= :startDate")
        boolean existsOverlappingBooking(@Param("vehicleId") Long vehicleId,
                        @Param("status") String status,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        @Query("SELECT b FROM Booking b WHERE b.status = 'CONFIRMED' " +
                        "AND b.startDate <= :date " +
                        "AND b.endDate >= :date")
        List<Booking> findActiveBookings(@Param("date") LocalDate date);
}