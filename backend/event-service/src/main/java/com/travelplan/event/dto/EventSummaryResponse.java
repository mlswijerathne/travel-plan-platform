package com.travelplan.event.dto;

import com.travelplan.event.model.enums.EventCategory;
import com.travelplan.event.model.enums.EventStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventSummaryResponse {
    private Long id;
    private String title;
    private EventCategory category;
    private String location;
    private OffsetDateTime startDateTime;
    private OffsetDateTime endDateTime;
    private Integer availableSeats;
    private BigDecimal ticketPrice;
    private String currency;
    private EventStatus status;
    private String imageUrl;
    private java.util.List<String> vibes;
    private boolean authenticCultural;
}
