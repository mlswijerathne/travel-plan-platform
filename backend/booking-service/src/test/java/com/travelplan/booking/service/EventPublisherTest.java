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
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventPublisherTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    private EventPublisher eventPublisher;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        eventPublisher = new EventPublisher(objectMapper, kafkaTemplate);
    }

    @Test
    void publishBookingCreated_sendsToBookingEventsTopic() {
        BookingResponse booking = buildSampleBooking();
        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenReturn(new CompletableFuture<>());

        eventPublisher.publishBookingCreated(booking);

        verify(kafkaTemplate, times(1)).send(eq("booking-events"), anyString(), anyString());
    }

    @Test
    void publishBookingConfirmed_sendsToBothTopics() {
        BookingResponse booking = buildSampleBooking();
        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenReturn(new CompletableFuture<>());

        eventPublisher.publishBookingConfirmed(booking);

        verify(kafkaTemplate, times(1)).send(eq("booking-events"), anyString(), anyString());
        verify(kafkaTemplate, times(1)).send(eq("booking-notifications"), anyString(), anyString());
    }

    @Test
    void publishBookingCancelled_sendsToBothTopics() {
        BookingResponse booking = buildSampleBooking();
        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenReturn(new CompletableFuture<>());

        eventPublisher.publishBookingCancelled(booking);

        verify(kafkaTemplate, times(1)).send(eq("booking-events"), anyString(), anyString());
        verify(kafkaTemplate, times(1)).send(eq("booking-notifications"), anyString(), anyString());
    }

    @Test
    void publishRefundProcessed_sendsToBookingEventsTopic() {
        BookingResponse booking = buildSampleBooking();
        booking.setRefundAmount(new BigDecimal("150.00"));
        booking.setRefundPolicy("FULL_REFUND");

        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenReturn(new CompletableFuture<>());

        eventPublisher.publishRefundProcessed(booking);

        verify(kafkaTemplate, times(1)).send(eq("booking-events"), anyString(), anyString());
    }

    @Test
    void publish_kafkaError_logsWithoutThrowing() {
        BookingResponse booking = buildSampleBooking();
        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Kafka unavailable"));

        // Should not throw
        eventPublisher.publishBookingCreated(booking);
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
}
