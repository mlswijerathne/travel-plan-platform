package com.travelplan.booking.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBookingItemStatusRequest {

    @NotBlank(message = "Status is required")
    private String status;

    private String reason;
}
