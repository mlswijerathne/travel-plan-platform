package com.travelplan.booking.dto;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityCheckResponse {
    private boolean available;
    private List<AvailabilityItemResponse> items;
}
