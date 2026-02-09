package com.travelplan.booking.repository;

import com.travelplan.booking.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Page<Booking> findByTouristId(String touristId, Pageable pageable);

    Page<Booking> findByTouristIdAndStatus(String touristId, String status, Pageable pageable);

    List<Booking> findByTouristIdAndStatus(String touristId, String status);

    Optional<Booking> findByBookingReference(String bookingReference);

    Optional<Booking> findByIdAndTouristId(Long id, String touristId);
}
