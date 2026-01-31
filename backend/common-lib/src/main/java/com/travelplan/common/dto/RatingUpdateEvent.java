package com.travelplan.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RatingUpdateEvent {
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
        private String entityType;
        private Long entityId;
        private BigDecimal newRating;
        private int reviewCount;
    }

    public static RatingUpdateEvent create(String entityType, Long entityId,
                                           BigDecimal newRating, int reviewCount) {
        return RatingUpdateEvent.builder()
                .eventType("review.rating.updated")
                .eventId(java.util.UUID.randomUUID().toString())
                .timestamp(Instant.now())
                .version("1.0")
                .source("review-service")
                .payload(Payload.builder()
                        .entityType(entityType)
                        .entityId(entityId)
                        .newRating(newRating)
                        .reviewCount(reviewCount)
                        .build())
                .build();
    }
}
