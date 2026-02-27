package com.travelplan.booking.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelplan.booking.config.KafkaConfig;
import com.travelplan.booking.dto.BookingEvent;
import com.travelplan.booking.dto.BookingResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

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
