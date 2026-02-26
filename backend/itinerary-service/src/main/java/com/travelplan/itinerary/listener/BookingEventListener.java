package com.travelplan.itinerary.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelplan.itinerary.event.BookingConfirmedEvent;
import com.travelplan.itinerary.service.BookingEventService;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingEventListener {
    private final BookingEventService bookingEventService;
    private final ObjectMapper objectMapper;

    @SqsListener("${aws.sqs.booking-events-queue:booking-events}")
    public void handleBookingConfirmedEvent(String message) {
        log.info("Received booking confirmed event: {}", message);

        try {
            BookingConfirmedEvent event = objectMapper.readValue(message, BookingConfirmedEvent.class);
            bookingEventService.handleBookingConfirmed(event);
            if (event.getPayload() != null && event.getPayload().getBookingId() != null) {
                log.info("Booking event processed successfully for booking {}", event.getPayload().getBookingId());
            } else {
                log.info("Booking event processed successfully");
            }
        } catch (Exception e) {
            log.error("Error processing booking confirmed event", e);
        }
    }
}
