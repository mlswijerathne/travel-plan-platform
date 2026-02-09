package com.travelplan.booking.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityItemResponse {
    private String providerType;
    private Long providerId;
    private boolean available;
    private String message;
}
