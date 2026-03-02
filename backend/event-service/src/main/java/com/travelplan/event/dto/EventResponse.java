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
public class EventResponse {
    private Long id;
    private String organizerId;
    private String organizerName;
    private String title;
    private String description;
    private EventCategory category;
    private String location;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private OffsetDateTime startDateTime;
    private OffsetDateTime endDateTime;
    private Integer totalCapacity;
    private Integer availableSeats;
    private BigDecimal ticketPrice;
    private String currency;
    private EventStatus status;
    private String imageUrl;
    private String tags;
    private java.util.List<String> vibes;
    private boolean authenticCultural;
    private java.util.List<TicketTierResponse> ticketTiers;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
