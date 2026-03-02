package com.travelplan.itinerary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO representing a location with coordinates for map display
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MapLocationDTO {
    private Long activityId;
    private String title;
    private String location;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Integer dayNumber;
    private Integer sequenceNumber;
    private String activityType;
    private String timeRange;
    private String description;
}
