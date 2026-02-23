package com.travelplan.event.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelTicketRequest {
    @NotBlank(message = "Cancellation reason is required")
    private String reason;
}
