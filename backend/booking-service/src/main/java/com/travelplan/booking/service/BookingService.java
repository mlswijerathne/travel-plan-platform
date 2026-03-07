package com.travelplan.booking.service;

import com.travelplan.booking.dto.*;
import com.travelplan.common.dto.PaginatedResponse;

public interface BookingService {

    BookingResponse createBooking(String touristId, CreateBookingRequest request);

    BookingResponse getBookingById(Long id, String touristId);

    PaginatedResponse<BookingResponse> getTouristBookings(String touristId, String status, int page, int size);

    BookingResponse cancelBooking(Long id, String touristId, CancelBookingRequest request);

    AvailabilityCheckResponse checkAvailability(AvailabilityCheckRequest request);

    BookingResponse updateBookingItemStatus(Long bookingId, Long itemId, UpdateBookingItemStatusRequest request);

    PaginatedResponse<BookingResponse> getProviderBookings(String providerType, Long providerId, String status, int page, int size);

    BookingResponse getBookingByReference(String bookingReference);

    void linkItinerary(Long bookingId, Long itineraryId);
}
