package com.travelplan.booking.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityCheckRequest {

    @NotEmpty(message = "At least one item is required for availability check")
    @Valid
    private List<AvailabilityItemRequest> items;
}
