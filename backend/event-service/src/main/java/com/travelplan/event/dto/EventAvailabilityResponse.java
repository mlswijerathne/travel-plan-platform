package com.travelplan.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventAvailabilityResponse {
    private Long eventId;
    private String eventTitle;
    private Integer totalCapacity;
    private Integer availableSeats;
    private boolean available;
    private BigDecimal ticketPrice;
    private String currency;
    private List<TicketTierResponse> ticketTiers;
}
