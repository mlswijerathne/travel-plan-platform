package com.travelplan.booking.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityItemRequest {

    @NotBlank(message = "Provider type is required")
    private String providerType;

    @NotNull(message = "Provider ID is required")
    private Long providerId;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity = 1;
}
