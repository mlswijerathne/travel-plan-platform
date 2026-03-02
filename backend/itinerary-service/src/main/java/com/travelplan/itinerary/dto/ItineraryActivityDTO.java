package com.travelplan.itinerary.dto;

import com.travelplan.itinerary.model.ActivityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItineraryActivityDTO {
    private Long id;
    private ActivityType activityType;
    private String providerType;
    private Long providerId;
    private String title;
    private String description;
    private LocalTime startTime;
    private LocalTime endTime;
    private String location;
    private BigDecimal estimatedCost;
    private Long bookingId;
    private Integer sortOrder;
}
