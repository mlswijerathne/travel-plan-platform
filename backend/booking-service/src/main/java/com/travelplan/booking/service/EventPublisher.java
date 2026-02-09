package com.travelplan.booking.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelplan.booking.dto.BookingEvent;
import com.travelplan.booking.dto.BookingResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventPublisher {

    private final ObjectMapper objectMapper;
    private final SqsClient sqsClient;

    @Value("${aws.sqs.booking-queue-url:}")
    private String bookingQueueUrl;

    @Value("${aws.sqs.notification-queue-url:}")
    private String notificationQueueUrl;

    public static final String BOOKING_CREATED = "booking.reservation.created";
    public static final String BOOKING_CONFIRMED = "booking.reservation.confirmed";
    public static final String BOOKING_CANCELLED = "booking.reservation.cancelled";
    public static final String BOOKING_REFUND_PROCESSED = "booking.refund.processed";

    public void publishBookingCreated(BookingResponse booking) {
        publish(BOOKING_CREATED, booking, bookingQueueUrl);
    }

    public void publishBookingConfirmed(BookingResponse booking) {
        publish(BOOKING_CONFIRMED, booking, bookingQueueUrl);
        publish(BOOKING_CONFIRMED, booking, notificationQueueUrl);
    }

    public void publishBookingCancelled(BookingResponse booking) {
        publish(BOOKING_CANCELLED, booking, bookingQueueUrl);
        publish(BOOKING_CANCELLED, booking, notificationQueueUrl);
    }

    public void publishRefundProcessed(BookingResponse booking) {
        publish(BOOKING_REFUND_PROCESSED, booking, bookingQueueUrl);
    }

    private void publish(String eventType, BookingResponse booking, String queueUrl) {
        if (sqsClient == null || queueUrl == null || queueUrl.isBlank()) {
            log.info("SQS not configured - event logged locally: type={}, bookingId={}", eventType, booking.getId());
            return;
        }

        try {
            BookingEvent event = BookingEvent.from(eventType, booking);
            String messageBody = objectMapper.writeValueAsString(event);

            sqsClient.sendMessage(SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(messageBody)
                    .messageGroupId(String.valueOf(booking.getId()))
                    .build());

            log.info("Published event: type={}, bookingId={}, eventId={}",
                    eventType, booking.getId(), event.getEventId());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event: type={}, bookingId={}", eventType, booking.getId(), e);
        } catch (Exception e) {
            log.error("Failed to publish event: type={}, bookingId={}", eventType, booking.getId(), e);
        }
    }
}
