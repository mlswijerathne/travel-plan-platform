package com.travelplan.booking.service.impl;

import com.travelplan.booking.client.HotelServiceClient;
import com.travelplan.booking.client.TourGuideServiceClient;
import com.travelplan.booking.client.VehicleServiceClient;
import com.travelplan.booking.dto.*;
import com.travelplan.booking.entity.Booking;
import com.travelplan.booking.entity.BookingItem;
import com.travelplan.booking.mapper.BookingMapper;
import com.travelplan.booking.repository.BookingItemRepository;
import com.travelplan.booking.repository.BookingRepository;
import com.travelplan.booking.service.BookingService;
import com.travelplan.booking.service.EventPublisher;
import com.travelplan.booking.service.RefundPolicyService;
import com.travelplan.booking.service.SagaOrchestrator;
import com.travelplan.common.dto.PaginatedResponse;
import com.travelplan.common.exception.ForbiddenException;
import com.travelplan.common.exception.ResourceNotFoundException;
import com.travelplan.common.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final BookingItemRepository bookingItemRepository;
    private final BookingMapper bookingMapper;
    private final HotelServiceClient hotelServiceClient;
    private final TourGuideServiceClient tourGuideServiceClient;
    private final VehicleServiceClient vehicleServiceClient;
    private final SagaOrchestrator sagaOrchestrator;
    private final EventPublisher eventPublisher;
    private final RefundPolicyService refundPolicyService;

    @Override
    @Transactional
    public BookingResponse createBooking(String touristId, CreateBookingRequest request) {
        log.info("Creating booking for tourist: {}", touristId);

        validateBookingDates(request.getStartDate(), request.getEndDate());

        // Build booking items and calculate total
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<BookingItem> bookingItems = new ArrayList<>();

        for (BookingItemRequest itemRequest : request.getItems()) {
            validateProviderType(itemRequest.getProviderType());

            BookingItem item = bookingMapper.toBookingItem(itemRequest);
            if (item.getStartDate() == null) {
                item.setStartDate(request.getStartDate());
            }
            if (item.getEndDate() == null) {
                item.setEndDate(request.getEndDate());
            }
            totalAmount = totalAmount.add(item.getSubtotal());
            bookingItems.add(item);
        }

        // Create and save booking with PENDING status
        Booking booking = Booking.builder()
                .touristId(touristId)
                .itineraryId(request.getItineraryId())
                .status("PENDING")
                .totalAmount(totalAmount)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .notes(request.getNotes())
                .bookingReference(generateBookingReference())
                .build();

        for (BookingItem item : bookingItems) {
            booking.addItem(item);
        }

        Booking savedBooking = bookingRepository.save(booking);
        log.info("Booking created: id={}, reference={}", savedBooking.getId(), savedBooking.getBookingReference());

        // Execute saga: pre-check availability + step-by-step provider confirmation
        SagaOrchestrator.SagaResult sagaResult = sagaOrchestrator.execute(savedBooking);

        if (!sagaResult.successful()) {
            // Saga failed or rolled back - update booking status
            savedBooking.setStatus("CANCELLED");
            savedBooking.setCancellationReason(sagaResult.message());
            savedBooking = bookingRepository.save(savedBooking);
            log.warn("Booking saga failed: id={}, reason={}", savedBooking.getId(), sagaResult.message());

            BookingResponse response = bookingMapper.toResponse(savedBooking);
            eventPublisher.publishBookingCancelled(response);
            throw new ValidationException("Booking failed: " + sagaResult.message());
        }

        // Saga succeeded - all providers confirmed
        savedBooking.setStatus("CONFIRMED");
        savedBooking = bookingRepository.save(savedBooking);

        BookingResponse response = bookingMapper.toResponse(savedBooking);
        eventPublisher.publishBookingCreated(response);
        eventPublisher.publishBookingConfirmed(response);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getBookingById(Long id, String touristId) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", id));

        if (!booking.getTouristId().equals(touristId)) {
            throw new ForbiddenException("You can only view your own bookings");
        }

        return bookingMapper.toResponse(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<BookingResponse> getTouristBookings(String touristId, String status, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Booking> bookingPage;
        if (status != null && !status.isBlank()) {
            bookingPage = bookingRepository.findByTouristIdAndStatus(touristId, status.toUpperCase(), pageRequest);
        } else {
            bookingPage = bookingRepository.findByTouristId(touristId, pageRequest);
        }

        List<BookingResponse> responses = bookingPage.getContent().stream()
                .map(bookingMapper::toResponse)
                .toList();

        return PaginatedResponse.of(responses, page, size, bookingPage.getTotalElements());
    }

    @Override
    @Transactional
    public BookingResponse cancelBooking(Long id, String touristId, CancelBookingRequest request) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", id));

        if (!booking.getTouristId().equals(touristId)) {
            throw new ForbiddenException("You can only cancel your own bookings");
        }

        String currentStatus = booking.getStatus();
        if ("CANCELLED".equals(currentStatus)) {
            throw new ValidationException("Booking is already cancelled");
        }
        if ("COMPLETED".equals(currentStatus)) {
            throw new ValidationException("Cannot cancel a completed booking");
        }

        // Calculate refund based on time-based policy
        RefundPolicyService.RefundResult refundResult =
                refundPolicyService.calculateRefund(booking.getTotalAmount(), booking.getStartDate());

        // Cancel all pending/confirmed items
        for (BookingItem item : booking.getItems()) {
            if (!"CANCELLED".equals(item.getStatus())) {
                item.setStatus("CANCELLED");
            }
        }

        booking.setStatus("CANCELLED");
        booking.setCancellationReason(request != null ? request.getReason() : null);
        booking.setRefundAmount(refundResult.getRefundAmount());
        booking.setRefundPolicy(refundResult.getRefundPolicy());

        Booking savedBooking = bookingRepository.save(booking);
        log.info("Booking cancelled: id={}, refundPolicy={}, refundAmount={}",
                id, refundResult.getRefundPolicy(), refundResult.getRefundAmount());

        BookingResponse response = bookingMapper.toResponse(savedBooking);
        eventPublisher.publishBookingCancelled(response);

        if (refundResult.getRefundAmount().compareTo(BigDecimal.ZERO) > 0) {
            eventPublisher.publishRefundProcessed(response);
        }

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public AvailabilityCheckResponse checkAvailability(AvailabilityCheckRequest request) {
        log.info("Checking availability for {} items", request.getItems().size());

        List<AvailabilityItemResponse> itemResponses = new ArrayList<>();
        boolean allAvailable = true;

        for (AvailabilityItemRequest item : request.getItems()) {
            AvailabilityItemResponse itemResponse = checkProviderAvailability(item);
            itemResponses.add(itemResponse);
            if (!itemResponse.isAvailable()) {
                allAvailable = false;
            }
        }

        return AvailabilityCheckResponse.builder()
                .available(allAvailable)
                .items(itemResponses)
                .build();
    }

    @Override
    @Transactional
    public BookingResponse updateBookingItemStatus(Long bookingId, Long itemId, UpdateBookingItemStatusRequest request) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        BookingItem targetItem = booking.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("BookingItem", "id", itemId));

        String newStatus = request.getStatus().toUpperCase();
        validateStatusTransition(targetItem.getStatus(), newStatus);
        targetItem.setStatus(newStatus);

        if ("CANCELLED".equals(newStatus) && request.getReason() != null) {
            log.info("Booking item {} declined: {}", itemId, request.getReason());
        }

        String previousBookingStatus = booking.getStatus();
        updateBookingStatusFromItems(booking);

        Booking savedBooking = bookingRepository.save(booking);
        BookingResponse response = bookingMapper.toResponse(savedBooking);

        // Publish events on booking-level status change
        if (!previousBookingStatus.equals(savedBooking.getStatus())) {
            if ("CONFIRMED".equals(savedBooking.getStatus())) {
                eventPublisher.publishBookingConfirmed(response);
            } else if ("CANCELLED".equals(savedBooking.getStatus())) {
                eventPublisher.publishBookingCancelled(response);
            }
        }

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<BookingResponse> getProviderBookings(String providerType, Long providerId, String status, int page, int size) {
        List<BookingItem> providerItems;
        if (status != null && !status.isBlank()) {
            providerItems = bookingItemRepository.findByProviderTypeAndProviderIdAndStatus(
                    providerType.toUpperCase(), providerId, status.toUpperCase());
        } else {
            providerItems = bookingItemRepository.findByProviderTypeAndProviderId(
                    providerType.toUpperCase(), providerId);
        }

        List<Long> bookingIds = providerItems.stream()
                .map(item -> item.getBooking().getId())
                .distinct()
                .toList();

        List<Booking> bookings = bookingRepository.findAllById(bookingIds);

        int start = page * size;
        int end = Math.min(start + size, bookings.size());
        List<BookingResponse> pagedResponses = (start < bookings.size())
                ? bookings.subList(start, end).stream().map(bookingMapper::toResponse).toList()
                : List.of();

        return PaginatedResponse.of(pagedResponses, page, size, bookings.size());
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getBookingByReference(String bookingReference) {
        Booking booking = bookingRepository.findByBookingReference(bookingReference)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "bookingReference", bookingReference));
        return bookingMapper.toResponse(booking);
    }

    // --- Private helper methods ---

    private void validateBookingDates(LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate) || endDate.isEqual(startDate)) {
            throw new ValidationException("End date must be after start date");
        }
    }

    private void validateProviderType(String providerType) {
        String upper = providerType.toUpperCase();
        if (!"HOTEL".equals(upper) && !"TOUR_GUIDE".equals(upper) && !"VEHICLE".equals(upper)) {
            throw new ValidationException("Invalid provider type: " + providerType + ". Must be HOTEL, TOUR_GUIDE, or VEHICLE");
        }
    }

    private void validateStatusTransition(String currentStatus, String newStatus) {
        if ("PENDING".equals(currentStatus) && ("CONFIRMED".equals(newStatus) || "CANCELLED".equals(newStatus))) {
            return;
        }
        if ("CONFIRMED".equals(currentStatus) && ("COMPLETED".equals(newStatus) || "CANCELLED".equals(newStatus))) {
            return;
        }
        throw new ValidationException(
                String.format("Invalid status transition from %s to %s", currentStatus, newStatus));
    }

    private void updateBookingStatusFromItems(Booking booking) {
        List<BookingItem> items = booking.getItems();
        if (items.isEmpty()) return;

        boolean allConfirmed = items.stream().allMatch(i -> "CONFIRMED".equals(i.getStatus()));
        boolean allCancelled = items.stream().allMatch(i -> "CANCELLED".equals(i.getStatus()));
        boolean allCompleted = items.stream().allMatch(i -> "COMPLETED".equals(i.getStatus()));

        if (allConfirmed) {
            booking.setStatus("CONFIRMED");
        } else if (allCancelled) {
            booking.setStatus("CANCELLED");
        } else if (allCompleted) {
            booking.setStatus("COMPLETED");
        }
    }

    private AvailabilityItemResponse checkProviderAvailability(AvailabilityItemRequest item) {
        String providerType = item.getProviderType().toUpperCase();
        String startDate = item.getStartDate().toString();
        String endDate = item.getEndDate().toString();

        try {
            switch (providerType) {
                case "HOTEL" -> hotelServiceClient.checkAvailability(item.getProviderId(), startDate, endDate);
                case "TOUR_GUIDE" -> tourGuideServiceClient.checkAvailability(item.getProviderId(), startDate, endDate);
                case "VEHICLE" -> vehicleServiceClient.checkAvailability(item.getProviderId(), startDate, endDate);
                default -> {
                    return AvailabilityItemResponse.builder()
                            .providerType(providerType).providerId(item.getProviderId())
                            .available(false).message("Unknown provider type: " + providerType).build();
                }
            }
            return AvailabilityItemResponse.builder()
                    .providerType(providerType).providerId(item.getProviderId())
                    .available(true).message("Available").build();
        } catch (Exception e) {
            log.warn("Availability check failed for {} id={}: {}", providerType, item.getProviderId(), e.getMessage());
            return AvailabilityItemResponse.builder()
                    .providerType(providerType).providerId(item.getProviderId())
                    .available(false).message("Provider service unavailable or provider not found").build();
        }
    }

    private String generateBookingReference() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uniquePart = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "TRP-" + datePart + "-" + uniquePart;
    }
}
