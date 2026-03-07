package com.travelplan.itinerary.service;

import com.travelplan.itinerary.client.BookingServiceClient;
import com.travelplan.itinerary.event.BookingConfirmedEvent;
import com.travelplan.itinerary.model.ActivityType;
import com.travelplan.itinerary.model.Itinerary;
import com.travelplan.itinerary.model.ItineraryDay;
import com.travelplan.itinerary.repository.ItineraryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BookingEventService {
    private final ItineraryRepository itineraryRepository;
    private final ItineraryDayService dayService;
    private final ItineraryActivityService activityService;
    private final BookingServiceClient bookingServiceClient;

    public void handleBookingConfirmed(BookingConfirmedEvent event) {
        log.info("Handling booking confirmed event: {}", event.getEventId());

        if (event.getPayload() == null || event.getPayload().getItems() == null) {
            log.warn("Booking event has no items, ignoring");
            return;
        }

        String touristId = event.getPayload().getTouristId();
        log.info("Processing {} booking items for tourist {}", event.getPayload().getItems().size(), touristId);

        // Check if any bookings already exist in any itinerary
        List<Itinerary> existingItineraries = itineraryRepository.findByTouristId(touristId);
        for (BookingConfirmedEvent.BookingItem item : event.getPayload().getItems()) {
            for (Itinerary itinerary : existingItineraries) {
                if (activityService.bookingExistsInItinerary(itinerary.getId(), event.getPayload().getBookingId())) {
                    log.info("Booking {} already exists in itinerary {}", event.getPayload().getBookingId(), itinerary.getId());
                    return;
                }
            }
        }

        // Find or create itinerary for the date range
        Itinerary itinerary = findOrCreateItinerary(event);
        
        // Add booking items to appropriate days
        addBookingItemsToItinerary(itinerary, event);

        // Link itinerary back to booking
        try {
            bookingServiceClient.linkItinerary(event.getPayload().getBookingId(), itinerary.getId());
            log.info("Linked booking {} to itinerary {}", event.getPayload().getBookingId(), itinerary.getId());
        } catch (Exception e) {
            log.warn("Failed to link itinerary {} back to booking {}: {}",
                    itinerary.getId(), event.getPayload().getBookingId(), e.getMessage());
        }

        log.info("Booking {} added to itinerary {}", event.getPayload().getBookingId(), itinerary.getId());
    }

    private Itinerary findOrCreateItinerary(BookingConfirmedEvent event) {
        BookingConfirmedEvent.BookingItem firstItem = event.getPayload().getItems().get(0);
        java.time.LocalDate startDate = java.time.LocalDate.parse(firstItem.getStartDate());
        java.time.LocalDate endDate = java.time.LocalDate.parse(firstItem.getEndDate());

        List<Itinerary> overlapping = itineraryRepository.findItinerariesByDateRange(
                event.getPayload().getTouristId(), 
                startDate
        );
        
        if (!overlapping.isEmpty()) {
            log.info("Found overlapping itinerary, using existing one");
            return overlapping.get(0);
        }

        log.info("Creating new itinerary for booking dates");
        return itineraryRepository.save(Itinerary.builder()
                .touristId(event.getPayload().getTouristId())
                .title("Trip")
                .startDate(startDate)
                .endDate(endDate)
                .status(com.travelplan.itinerary.model.TripStatus.ACTIVE)
                .build());
    }

    private void addBookingItemsToItinerary(Itinerary itinerary, BookingConfirmedEvent event) {
        for (BookingConfirmedEvent.BookingItem item : event.getPayload().getItems()) {
            java.time.LocalDate startDate = java.time.LocalDate.parse(item.getStartDate());
            java.time.LocalDate endDate = java.time.LocalDate.parse(item.getEndDate());
            
            ActivityType activityType = mapProviderTypeToActivityType(item.getProviderType());

            // Add item to each day it spans
            java.time.LocalDate currentDate = startDate;
            while (!currentDate.isAfter(endDate.minusDays(1))) {
                ItineraryDay day = dayService.getOrCreateDay(itinerary.getId(), currentDate);
                
                activityService.addBookingToItinerary(
                        itinerary.getId(),
                        day.getId(),
                        item.getItemName(),
                        null, // startTime
                        null, // endTime
                        null, // location
                        event.getPayload().getBookingId(),
                        item.getProviderType(),
                        item.getProviderId(),
                        activityType
                );

                currentDate = currentDate.plusDays(1);
            }
        }
    }

    private ActivityType mapProviderTypeToActivityType(String providerType) {
        if (providerType == null) {
            return ActivityType.ACCOMMODATION;
        }

        return switch (providerType.toUpperCase()) {
            case "HOTEL" -> ActivityType.ACCOMMODATION;
            case "VEHICLE", "TRANSPORT" -> ActivityType.TRANSPORT;
            case "ACTIVITY", "TOUR" -> ActivityType.ACTIVITY;
            case "TOUR_GUIDE", "GUIDE" -> ActivityType.GUIDE;
            default -> ActivityType.CUSTOM;
        };
    }
}
