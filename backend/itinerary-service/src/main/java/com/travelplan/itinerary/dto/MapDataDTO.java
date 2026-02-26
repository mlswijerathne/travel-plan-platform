package com.travelplan.itinerary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * DTO for map visualization of an itinerary
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MapDataDTO {
    private Long itineraryId;
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private Map<String, Object> geoJsonFeatures;
    private Map<String, Object> routeGeometry;
    private List<MapLocationDTO> locations = new ArrayList<>();
    private Map<Integer, String> dayColors;
    private Double centerLatitude;
    private Double centerLongitude;
    private Integer zoomLevel;
}
