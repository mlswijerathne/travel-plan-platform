package com.travelplan.booking.controller;

import com.travelplan.booking.dto.*;
import com.travelplan.booking.service.BookingService;
import com.travelplan.common.dto.ApiResponse;
import com.travelplan.common.dto.PaginatedResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(
            @Valid @RequestBody CreateBookingRequest request,
            Authentication authentication) {
        String touristId = authentication.getName();
        log.info("Create booking request from tourist: {}", touristId);
        BookingResponse response = bookingService.createBooking(touristId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ApiResponse<BookingResponse> getBookingById(
            @PathVariable Long id,
            Authentication authentication) {
        String touristId = authentication.getName();
        return ApiResponse.success(bookingService.getBookingById(id, touristId));
    }

    @GetMapping
    public PaginatedResponse<BookingResponse> getTouristBookings(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        String touristId = authentication.getName();
        return bookingService.getTouristBookings(touristId, status, page, size);
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<BookingResponse> cancelBooking(
            @PathVariable Long id,
            @RequestBody(required = false) CancelBookingRequest request,
            Authentication authentication) {
        String touristId = authentication.getName();
        log.info("Cancel booking request: bookingId={}, touristId={}", id, touristId);
        return ApiResponse.success(bookingService.cancelBooking(id, touristId, request));
    }

    @PostMapping("/availability-check")
    public ApiResponse<AvailabilityCheckResponse> checkAvailability(
            @Valid @RequestBody AvailabilityCheckRequest request) {
        return ApiResponse.success(bookingService.checkAvailability(request));
    }

    @PutMapping("/{bookingId}/items/{itemId}/status")
    public ApiResponse<BookingResponse> updateBookingItemStatus(
            @PathVariable Long bookingId,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateBookingItemStatusRequest request) {
        log.info("Update item status: bookingId={}, itemId={}, newStatus={}", bookingId, itemId, request.getStatus());
        return ApiResponse.success(bookingService.updateBookingItemStatus(bookingId, itemId, request));
    }

    @GetMapping("/provider/{providerType}/{providerId}")
    public PaginatedResponse<BookingResponse> getProviderBookings(
            @PathVariable String providerType,
            @PathVariable Long providerId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return bookingService.getProviderBookings(providerType, providerId, status, page, size);
    }

    @GetMapping("/reference/{bookingReference}")
    public ApiResponse<BookingResponse> getBookingByReference(
            @PathVariable String bookingReference) {
        return ApiResponse.success(bookingService.getBookingByReference(bookingReference));
    }

    @PutMapping("/{bookingId}/itinerary/{itineraryId}")
    public ResponseEntity<Void> linkItinerary(
            @PathVariable Long bookingId,
            @PathVariable Long itineraryId) {
        log.info("Linking booking {} to itinerary {}", bookingId, itineraryId);
        bookingService.linkItinerary(bookingId, itineraryId);
        return ResponseEntity.ok().build();
    }
}
