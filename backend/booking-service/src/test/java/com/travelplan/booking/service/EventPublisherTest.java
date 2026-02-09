package com.travelplan.booking.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.travelplan.booking.dto.BookingItemResponse;
import com.travelplan.booking.dto.BookingResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventPublisherTest {

    @Mock
    private SqsClient sqsClient;

    private EventPublisher eventPublisher;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        eventPublisher = new EventPublisher(objectMapper, sqsClient);

        // Set queue URLs via reflection since @Value won't work in unit test
        setField(eventPublisher, "bookingQueueUrl", "https://sqs.ap-south-1.amazonaws.com/123/booking-queue");
        setField(eventPublisher, "notificationQueueUrl", "https://sqs.ap-south-1.amazonaws.com/123/notification-queue");
    }

    @Test
    void publishBookingCreated_sendsToBookingQueue() {
        BookingResponse booking = buildSampleBooking();
        when(sqsClient.sendMessage(any(SendMessageRequest.class)))
                .thenReturn(SendMessageResponse.builder().messageId("msg-1").build());

        eventPublisher.publishBookingCreated(booking);

        verify(sqsClient, times(1)).sendMessage(any(SendMessageRequest.class));
    }

    @Test
    void publishBookingConfirmed_sendsToBothQueues() {
        BookingResponse booking = buildSampleBooking();
        when(sqsClient.sendMessage(any(SendMessageRequest.class)))
                .thenReturn(SendMessageResponse.builder().messageId("msg-1").build());

        eventPublisher.publishBookingConfirmed(booking);

        verify(sqsClient, times(2)).sendMessage(any(SendMessageRequest.class));
    }

    @Test
    void publishBookingCancelled_sendsToBothQueues() {
        BookingResponse booking = buildSampleBooking();
        when(sqsClient.sendMessage(any(SendMessageRequest.class)))
                .thenReturn(SendMessageResponse.builder().messageId("msg-1").build());

        eventPublisher.publishBookingCancelled(booking);

        verify(sqsClient, times(2)).sendMessage(any(SendMessageRequest.class));
    }

    @Test
    void publish_noSqsClient_logsWithoutError() throws Exception {
        EventPublisher noSqsPublisher = new EventPublisher(objectMapper, null);
        setField(noSqsPublisher, "bookingQueueUrl", "");
        setField(noSqsPublisher, "notificationQueueUrl", "");

        // Should not throw
        noSqsPublisher.publishBookingCreated(buildSampleBooking());

        verifyNoInteractions(sqsClient);
    }

    @Test
    void publish_emptyQueueUrl_logsWithoutError() throws Exception {
        setField(eventPublisher, "bookingQueueUrl", "");

        eventPublisher.publishBookingCreated(buildSampleBooking());

        verifyNoInteractions(sqsClient);
    }

    @Test
    void publishRefundProcessed_sendsToBookingQueue() {
        BookingResponse booking = buildSampleBooking();
        booking.setRefundAmount(new BigDecimal("150.00"));
        booking.setRefundPolicy("FULL_REFUND");

        when(sqsClient.sendMessage(any(SendMessageRequest.class)))
                .thenReturn(SendMessageResponse.builder().messageId("msg-1").build());

        eventPublisher.publishRefundProcessed(booking);

        verify(sqsClient, times(1)).sendMessage(any(SendMessageRequest.class));
    }

    private BookingResponse buildSampleBooking() {
        return BookingResponse.builder()
                .id(1L).touristId("tourist-123").bookingReference("TRP-20260210-ABC123")
                .status("CONFIRMED").totalAmount(new BigDecimal("300.00"))
                .startDate(LocalDate.now().plusDays(5)).endDate(LocalDate.now().plusDays(10))
                .items(List.of(BookingItemResponse.builder()
                        .providerType("HOTEL").providerId(10L).itemName("Room")
                        .subtotal(new BigDecimal("300.00")).status("CONFIRMED")
                        .startDate(LocalDate.now().plusDays(5)).endDate(LocalDate.now().plusDays(10))
                        .build()))
                .build();
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
