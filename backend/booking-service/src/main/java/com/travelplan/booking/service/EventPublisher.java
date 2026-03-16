package com.travelplan.booking.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelplan.booking.config.KafkaConfig;
import com.travelplan.booking.dto.BookingEvent;
import com.travelplan.booking.dto.BookingItemResponse;
import com.travelplan.booking.dto.BookingResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventPublisher {

    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public static final String BOOKING_CREATED = "booking.reservation.created";
    public static final String BOOKING_CONFIRMED = "booking.reservation.confirmed";
    public static final String BOOKING_CANCELLED = "booking.reservation.cancelled";
    public static final String BOOKING_REFUND_PROCESSED = "booking.refund.processed";

    public void publishBookingCreated(BookingResponse booking) {
        publish(BOOKING_CREATED, booking, KafkaConfig.TOPIC_BOOKING_EVENTS);
    }

    public void publishBookingConfirmed(BookingResponse booking) {
        publish(BOOKING_CONFIRMED, booking, KafkaConfig.TOPIC_BOOKING_EVENTS);
        publish(BOOKING_CONFIRMED, booking, KafkaConfig.TOPIC_BOOKING_NOTIFICATIONS);
    }

    public void publishBookingCancelled(BookingResponse booking) {
        publish(BOOKING_CANCELLED, booking, KafkaConfig.TOPIC_BOOKING_EVENTS);
        publish(BOOKING_CANCELLED, booking, KafkaConfig.TOPIC_BOOKING_NOTIFICATIONS);
    }

    public void publishRefundProcessed(BookingResponse booking) {
        publish(BOOKING_REFUND_PROCESSED, booking, KafkaConfig.TOPIC_BOOKING_EVENTS);
    }

    public void publishTripCompleted(BookingResponse booking) {
        try {
            List<Map<String, Object>> bookingItems = booking.getItems().stream()
                    .filter(item -> !"CANCELLED".equals(item.getStatus()))
                    .map(item -> {
                        Map<String, Object> bi = new HashMap<>();
                        bi.put("entityType", item.getProviderType());
                        bi.put("entityId", item.getProviderId());
                        bi.put("entityName", item.getItemName());
                        bi.put("bookingId", booking.getId());
                        return bi;
                    })
                    .toList();

            Map<String, Object> payload = new HashMap<>();
            payload.put("touristId", booking.getTouristId());
            payload.put("itineraryId", booking.getItineraryId());
            payload.put("tripEndDate", booking.getEndDate() != null ? booking.getEndDate().toString() : null);
            payload.put("bookingItems", bookingItems);

            Map<String, Object> event = new HashMap<>();
            event.put("eventType", "itinerary.trip.completed");
            event.put("eventId", UUID.randomUUID().toString());
            event.put("timestamp", Instant.now().toString());
            event.put("version", "1.0");
            event.put("source", "booking-service");
            event.put("payload", payload);

            String messageBody = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(KafkaConfig.TOPIC_TRIP_COMPLETION, String.valueOf(booking.getId()), messageBody);
            log.info("Published trip-completion event for bookingId={}, touristId={}", booking.getId(), booking.getTouristId());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize trip-completion event for bookingId={}", booking.getId(), e);
        } catch (Exception e) {
            log.error("Failed to publish trip-completion event for bookingId={}", booking.getId(), e);
        }
    }

    private void publish(String eventType, BookingResponse booking, String topic) {
        try {
            BookingEvent event = BookingEvent.from(eventType, booking);
            String messageBody = objectMapper.writeValueAsString(event);
            String key = String.valueOf(booking.getId());

            kafkaTemplate.send(topic, key, messageBody);

            log.info("Published event: type={}, bookingId={}, eventId={}, topic={}",
                    eventType, booking.getId(), event.getEventId(), topic);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event: type={}, bookingId={}", eventType, booking.getId(), e);
        } catch (Exception e) {
            log.error("Failed to publish event: type={}, bookingId={}", eventType, booking.getId(), e);
        }
    }
}
