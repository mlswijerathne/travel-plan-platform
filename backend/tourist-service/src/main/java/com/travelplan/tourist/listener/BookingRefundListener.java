package com.travelplan.tourist.listener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelplan.tourist.service.TouristService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingRefundListener {

    private final TouristService touristService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "booking-events", groupId = "tourist-service-group")
    public void handleBookingEvent(String message) {
        try {
            JsonNode event = objectMapper.readTree(message);
            String eventType = event.path("eventType").asText();

            if (!"booking.refund.processed".equals(eventType)) {
                return;
            }

            JsonNode payload = event.path("payload");
            String touristId = payload.path("touristId").asText();
            BigDecimal refundAmount = new BigDecimal(payload.path("refundAmount").asText());
            String bookingReference = payload.path("bookingReference").asText();
            String refundPolicy = payload.path("refundPolicy").asText();

            if (refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
                log.debug("Skipping zero/negative refund for booking {}", bookingReference);
                return;
            }

            String description = String.format("Refund for booking %s (%s)", bookingReference, refundPolicy);
            touristService.creditWallet(touristId, refundAmount, description, bookingReference);
            log.info("Wallet credited for tourist {} — refund {} for booking {}",
                    touristId, refundAmount, bookingReference);

        } catch (Exception e) {
            log.error("Error processing booking refund event", e);
            throw new RuntimeException("Failed to process booking refund event", e);
        }
    }
}
