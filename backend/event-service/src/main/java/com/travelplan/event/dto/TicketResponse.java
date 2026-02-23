package com.travelplan.event.dto;

import com.travelplan.event.model.enums.RegistrationStatus;
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
public class TicketResponse {
    private Long id;
    private String ticketNumber;
    private Long eventId;
    private String eventTitle;
    private String touristId;
    private Integer numberOfTickets;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
    private RegistrationStatus status;
    private OffsetDateTime registeredAt;
    private OffsetDateTime eventStartDateTime;
    private String eventLocation;
}
