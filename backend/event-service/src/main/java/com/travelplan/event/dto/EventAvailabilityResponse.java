package com.travelplan.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventAvailabilityResponse {
    private Long eventId;
    private String eventTitle;
    private Integer totalCapacity;
    private Integer availableSeats;
    private boolean isAvailable;
    private BigDecimal ticketPrice;
    private String currency;
}
