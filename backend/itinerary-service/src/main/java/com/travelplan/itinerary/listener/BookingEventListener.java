package com.travelplan.itinerary.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelplan.itinerary.event.BookingConfirmedEvent;
import com.travelplan.itinerary.service.BookingEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingEventListener {
    private final BookingEventService bookingEventService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "booking-events", groupId = "itinerary-service-group")
    public void handleBookingEvent(String message) {
        log.info("Received booking event: {}", message);

        try {
            BookingConfirmedEvent event = objectMapper.readValue(message, BookingConfirmedEvent.class);

            // Only process confirmed booking events
            if (!"booking.reservation.confirmed".equals(event.getEventType())) {
                log.debug("Ignoring non-confirmed booking event: {}", event.getEventType());
                return;
            }

            bookingEventService.handleBookingConfirmed(event);
            if (event.getPayload() != null && event.getPayload().getBookingId() != null) {
                log.info("Booking event processed successfully for booking {}", event.getPayload().getBookingId());
            } else {
                log.info("Booking event processed successfully");
            }
        } catch (Exception e) {
            log.error("Error processing booking event", e);
        }
    }
}
