package com.travelplan.itinerary.job;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelplan.itinerary.config.KafkaEventPublisher;
import com.travelplan.itinerary.event.TripCompletedEvent;
import com.travelplan.itinerary.model.ActivityType;
import com.travelplan.itinerary.model.Itinerary;
import com.travelplan.itinerary.model.TripStatus;
import com.travelplan.itinerary.repository.ItineraryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class TripCompletionJob {
    private final ItineraryRepository itineraryRepository;
    private final KafkaEventPublisher kafkaEventPublisher;
    private final ObjectMapper objectMapper;

    @Scheduled(cron = "${scheduling.trip-completion.cron:0 0 0 * * *}")
    @Transactional
    public void detectAndProcessCompletedTrips() {
        log.info("Starting trip completion detection job");

        try {
            LocalDate currentDate = LocalDate.now();
            List<Itinerary> completedTrips = itineraryRepository.findCompletedTrips(currentDate);

            log.info("Found {} trips that ended before today", completedTrips.size());

            for (Itinerary trip : completedTrips) {
                processCompletedTrip(trip);
            }

            log.info("Trip completion detection job completed successfully");
        } catch (Exception e) {
            log.error("Error in trip completion detection job", e);
        }
    }

    private void processCompletedTrip(Itinerary trip) {
        try {
            log.info("Processing completed trip: {}", trip.getId());

            // Update status
            trip.setStatus(TripStatus.COMPLETED);
            itineraryRepository.save(trip);

            // Extract booking items from activities
            List<TripCompletedEvent.BookingItem> bookingItems = extractBookingItems(trip);

            // Publish trip completed event using factory method
            TripCompletedEvent event = TripCompletedEvent.of(
                    trip.getTouristId(),
                    trip.getId(),
                    trip.getEndDate(),
                    bookingItems
            );

            kafkaEventPublisher.publishTripCompletedEvent(objectMapper.writeValueAsString(event));
            log.info("Trip {} marked as completed and event published", trip.getId());

        } catch (Exception e) {
            log.error("Error processing completed trip {}", trip.getId(), e);
        }
    }

    private List<TripCompletedEvent.BookingItem> extractBookingItems(Itinerary trip) {
        return trip.getDays().stream()
                .flatMap(day -> day.getActivities().stream())
                .filter(activity -> activity.getBookingId() != null)
                .filter(activity -> activity.getProviderId() != null)
                .filter(activity -> mapActivityTypeToEntityType(activity.getActivityType()) != null)
                .map(activity -> TripCompletedEvent.BookingItem.builder()
                        .entityType(mapActivityTypeToEntityType(activity.getActivityType()))
                        .entityId(activity.getProviderId())
                        .entityName(activity.getTitle())
                        .bookingId(activity.getBookingId())
                        .build())
                .collect(Collectors.toMap(
                        item -> item.getBookingId() + ":" + item.getEntityType() + ":" + item.getEntityId(),
                        item -> item,
                        (a, b) -> a
                ))
                .values().stream()
                .collect(Collectors.toList());
    }

    private String mapActivityTypeToEntityType(ActivityType activityType) {
        return switch (activityType) {
            case ACCOMMODATION -> "HOTEL";
            case TRANSPORT -> "VEHICLE";
            case GUIDE -> "TOUR_GUIDE";
            default -> null; // ACTIVITY and CUSTOM have no reviewable provider
        };
    }

    private List<String> extractProviderIds(Itinerary trip) {
        return trip.getDays().stream()
                .flatMap(day -> day.getActivities().stream())
                .filter(activity -> activity.getProviderId() != null)
                .map(activity -> activity.getProviderId().toString())
                .distinct()
                .collect(Collectors.toList());
    }
}
