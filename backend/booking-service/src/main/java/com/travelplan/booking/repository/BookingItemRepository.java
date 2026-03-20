package com.travelplan.booking.repository;

import com.travelplan.booking.entity.BookingItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingItemRepository extends JpaRepository<BookingItem, Long> {

    List<BookingItem> findByProviderTypeAndProviderIdAndStatus(String providerType, Long providerId, String status);

    List<BookingItem> findByBookingId(Long bookingId);

    List<BookingItem> findByProviderTypeAndProviderId(String providerType, Long providerId);

    @Query("SELECT COUNT(bi) FROM BookingItem bi WHERE bi.providerType = :providerType AND bi.providerId = :providerId AND bi.status IN ('PENDING', 'CONFIRMED') AND bi.startDate < :endDate AND bi.endDate > :startDate")
    long countConfirmedOverlappingBookings(
            @Param("providerType") String providerType,
            @Param("providerId") Long providerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
