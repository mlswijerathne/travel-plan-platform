package com.travelplan.booking.service;

import com.travelplan.booking.client.HotelServiceClient;
import com.travelplan.booking.client.TourGuideServiceClient;
import com.travelplan.booking.client.VehicleServiceClient;
import com.travelplan.booking.dto.AvailabilityCheckRequest;
import com.travelplan.booking.dto.AvailabilityCheckResponse;
import com.travelplan.booking.dto.AvailabilityItemRequest;
import com.travelplan.booking.dto.AvailabilityItemResponse;
import com.travelplan.booking.entity.Booking;
import com.travelplan.booking.entity.BookingItem;
import com.travelplan.booking.entity.SagaOrchestration;
import com.travelplan.booking.repository.SagaOrchestrationRepository;
import com.travelplan.common.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SagaOrchestrator {

    private final SagaOrchestrationRepository sagaRepository;
    private final HotelServiceClient hotelServiceClient;
    private final TourGuideServiceClient tourGuideServiceClient;
    private final VehicleServiceClient vehicleServiceClient;

    /**
     * Executes the full saga for a multi-provider booking:
     * 1. Pre-check availability for ALL providers
     * 2. Initiate saga record
     * 3. Confirm each provider step-by-step
     * 4. On failure at any step, compensate all completed steps
     */
    @Transactional
    public SagaResult execute(Booking booking) {
        List<BookingItem> items = booking.getItems();
        if (items.isEmpty()) {
            return SagaResult.success();
        }

        log.info("Starting saga for booking id={}, items={}", booking.getId(), items.size());

        // Step 1: Pre-check availability for ALL providers
        AvailabilityCheckResponse availabilityResponse = preCheckAvailability(items);
        if (!availabilityResponse.isAvailable()) {
            List<String> unavailable = availabilityResponse.getItems().stream()
                    .filter(i -> !i.isAvailable())
                    .map(i -> i.getProviderType() + " (id=" + i.getProviderId() + "): " + i.getMessage())
                    .toList();
            String message = "Availability check failed: " + String.join("; ", unavailable);
            log.warn("Saga pre-check failed for booking id={}: {}", booking.getId(), message);
            return SagaResult.failed(message);
        }

        // Step 2: Create saga record
        SagaOrchestration saga = SagaOrchestration.builder()
                .bookingId(booking.getId())
                .sagaState("IN_PROGRESS")
                .currentStep(0)
                .totalSteps(items.size())
                .build();
        saga = sagaRepository.save(saga);

        // Step 3: Confirm each provider step-by-step
        List<BookingItem> confirmedItems = new ArrayList<>();

        for (int i = 0; i < items.size(); i++) {
            BookingItem item = items.get(i);
            try {
                confirmWithProvider(item);
                item.setStatus("CONFIRMED");
                confirmedItems.add(item);
                saga.markStepCompleted(i + 1, item.getProviderType() + ":" + item.getProviderId());
                sagaRepository.save(saga);
                log.info("Saga step {}/{} completed: {} id={}",
                        i + 1, items.size(), item.getProviderType(), item.getProviderId());
            } catch (Exception e) {
                log.error("Saga step {}/{} failed: {} id={} - {}",
                        i + 1, items.size(), item.getProviderType(), item.getProviderId(), e.getMessage());

                // Step 4: Compensate all completed steps
                saga.markRollingBack();
                sagaRepository.save(saga);

                compensate(confirmedItems, booking.getId());
                item.setStatus("CANCELLED");

                // Mark remaining items as cancelled
                for (int j = i + 1; j < items.size(); j++) {
                    items.get(j).setStatus("CANCELLED");
                }

                saga.markRolledBack();
                saga.setFailureReason(item.getProviderType() + " (id=" + item.getProviderId() + ") failed: " + e.getMessage());
                sagaRepository.save(saga);

                return SagaResult.rolledBack(
                        "Booking failed at " + item.getProviderType() + " (id=" + item.getProviderId() + "): " + e.getMessage());
            }
        }

        // All steps completed
        saga.markCompleted();
        sagaRepository.save(saga);
        log.info("Saga completed successfully for booking id={}", booking.getId());

        return SagaResult.success();
    }

    /**
     * Pre-checks availability across all providers before starting the saga.
     */
    private AvailabilityCheckResponse preCheckAvailability(List<BookingItem> items) {
        List<AvailabilityItemResponse> responses = new ArrayList<>();
        boolean allAvailable = true;

        for (BookingItem item : items) {
            AvailabilityItemResponse response = checkSingleProviderAvailability(item);
            responses.add(response);
            if (!response.isAvailable()) {
                allAvailable = false;
            }
        }

        return AvailabilityCheckResponse.builder()
                .available(allAvailable)
                .items(responses)
                .build();
    }

    private AvailabilityItemResponse checkSingleProviderAvailability(BookingItem item) {
        String providerType = item.getProviderType();
        String startDate = item.getStartDate() != null ? item.getStartDate().toString() : "";
        String endDate = item.getEndDate() != null ? item.getEndDate().toString() : "";

        try {
            switch (providerType) {
                case "HOTEL" -> hotelServiceClient.checkAvailability(item.getProviderId(), startDate, endDate);
                case "TOUR_GUIDE" -> tourGuideServiceClient.checkAvailability(item.getProviderId(), startDate, endDate);
                case "VEHICLE" -> vehicleServiceClient.checkAvailability(item.getProviderId(), startDate, endDate);
                default -> {
                    return AvailabilityItemResponse.builder()
                            .providerType(providerType).providerId(item.getProviderId())
                            .available(false).message("Unknown provider type").build();
                }
            }
            return AvailabilityItemResponse.builder()
                    .providerType(providerType).providerId(item.getProviderId())
                    .available(true).message("Available").build();
        } catch (Exception e) {
            return AvailabilityItemResponse.builder()
                    .providerType(providerType).providerId(item.getProviderId())
                    .available(false).message("Unavailable: " + e.getMessage()).build();
        }
    }

    /**
     * Confirms a booking item with the specific provider service.
     */
    private void confirmWithProvider(BookingItem item) {
        String providerType = item.getProviderType();
        String startDate = item.getStartDate() != null ? item.getStartDate().toString() : "";
        String endDate = item.getEndDate() != null ? item.getEndDate().toString() : "";

        // Verify the provider exists (acts as confirmation in current architecture)
        switch (providerType) {
            case "HOTEL" -> hotelServiceClient.getHotelById(item.getProviderId());
            case "TOUR_GUIDE" -> tourGuideServiceClient.getTourGuideById(item.getProviderId());
            case "VEHICLE" -> vehicleServiceClient.getVehicleById(item.getProviderId());
            default -> throw new ValidationException("Unknown provider type: " + providerType);
        }
    }

    /**
     * Compensating transactions: cancels all previously confirmed items.
     */
    private void compensate(List<BookingItem> confirmedItems, Long bookingId) {
        log.warn("Compensating {} confirmed items for booking id={}", confirmedItems.size(), bookingId);

        for (BookingItem item : confirmedItems) {
            try {
                item.setStatus("CANCELLED");
                log.info("Compensated {} id={} for booking id={}",
                        item.getProviderType(), item.getProviderId(), bookingId);
            } catch (Exception e) {
                log.error("Compensation failed for {} id={} on booking id={}: {}",
                        item.getProviderType(), item.getProviderId(), bookingId, e.getMessage());
            }
        }
    }

    /**
     * Result of a saga execution.
     */
    public record SagaResult(boolean successful, boolean rolledBack, String message) {
        public static SagaResult success() {
            return new SagaResult(true, false, null);
        }

        public static SagaResult failed(String message) {
            return new SagaResult(false, false, message);
        }

        public static SagaResult rolledBack(String message) {
            return new SagaResult(false, true, message);
        }
    }
}
