package com.travelplan.event.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookTicketRequest {

    @NotNull(message = "Number of tickets is required")
    @Min(value = 1, message = "Must book at least 1 ticket")
    private Integer numberOfTickets;

    private Long ticketTierId;

    private String notes;
}
