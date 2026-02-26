package com.travelplan.booking.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingEvent {

    private String eventType;
    private String eventId;
    private Instant timestamp;
    private String version;
    private String source;
    private Payload payload;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Payload {
        private Long bookingId;
        private String bookingReference;
        private String touristId;
        private BigDecimal totalAmount;
        private BigDecimal refundAmount;
        private String refundPolicy;
        private String cancellationReason;
        private List<BookingItemPayload> items;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookingItemPayload {
        private String providerType;
        private Long providerId;
        private String itemName;
        private BigDecimal subtotal;
        private String startDate;
        private String endDate;
        private String status;
    }

    public static BookingEvent from(String eventType, BookingResponse booking) {
        List<BookingItemPayload> itemPayloads = List.of();
        if (booking.getItems() != null) {
            itemPayloads = booking.getItems().stream()
                    .map(item -> BookingItemPayload.builder()
                            .providerType(item.getProviderType())
                            .providerId(item.getProviderId())
                            .itemName(item.getItemName())
                            .subtotal(item.getSubtotal())
                            .startDate(item.getStartDate() != null ? item.getStartDate().toString() : null)
                            .endDate(item.getEndDate() != null ? item.getEndDate().toString() : null)
                            .status(item.getStatus())
                            .build())
                    .toList();
        }

        return BookingEvent.builder()
                .eventType(eventType)
                .eventId(UUID.randomUUID().toString())
                .timestamp(Instant.now())
                .version("1.0")
                .source("booking-service")
                .payload(Payload.builder()
                        .bookingId(booking.getId())
                        .bookingReference(booking.getBookingReference())
                        .touristId(booking.getTouristId())
                        .totalAmount(booking.getTotalAmount())
                        .refundAmount(booking.getRefundAmount())
                        .refundPolicy(booking.getRefundPolicy())
                        .cancellationReason(booking.getCancellationReason())
                        .items(itemPayloads)
                        .build())
                .build();
    }
}
