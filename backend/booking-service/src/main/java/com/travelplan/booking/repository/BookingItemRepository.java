package com.travelplan.booking.repository;

import com.travelplan.booking.entity.BookingItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingItemRepository extends JpaRepository<BookingItem, Long> {

    List<BookingItem> findByProviderTypeAndProviderIdAndStatus(String providerType, Long providerId, String status);

    List<BookingItem> findByBookingId(Long bookingId);

    List<BookingItem> findByProviderTypeAndProviderId(String providerType, Long providerId);
}
