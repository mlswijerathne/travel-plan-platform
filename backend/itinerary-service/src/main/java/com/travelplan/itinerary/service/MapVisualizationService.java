package com.travelplan.itinerary.service;

import com.travelplan.itinerary.dto.MapDataDTO;
import com.travelplan.itinerary.dto.MapLocationDTO;
import com.travelplan.itinerary.model.Itinerary;
import com.travelplan.itinerary.model.ItineraryActivity;
import com.travelplan.itinerary.model.ItineraryDay;
import com.travelplan.itinerary.repository.ItineraryRepository;
import com.travelplan.itinerary.util.GeoJsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for generating map visualization data for itineraries
 * Handles GeoJSON generation, route creation, and location-based queries
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MapVisualizationService {
    private final ItineraryRepository itineraryRepository;

    private static final String[] DAY_COLORS = {
            "#FF6B6B", "#4ECDC4", "#45B7D1", "#FFA07A", "#98D8C8",
            "#F7DC6F", "#BB8FCE", "#85C1E2", "#F8B088", "#52C9A9"
    };

    /**
     * Generate complete map data for an itinerary
     */
    public MapDataDTO generateItineraryMapData(Long itineraryId, String touristId) {
        log.info("Generating map data for itinerary {} for tourist {}", itineraryId, touristId);

        Itinerary itinerary = itineraryRepository.findByIdAndTouristId(itineraryId, touristId)
                .orElseThrow(() -> new IllegalArgumentException("Itinerary not found or access denied"));

        return buildMapData(itinerary, null);
    }

    /**
     * Generate map data for a specific day within an itinerary
     */
    public MapDataDTO generateDayMapData(Long itineraryId, Long dayId, String touristId) {
        log.info("Generating map data for day {} in itinerary {}", dayId, itineraryId);

        Itinerary itinerary = itineraryRepository.findByIdAndTouristId(itineraryId, touristId)
                .orElseThrow(() -> new IllegalArgumentException("Itinerary not found or access denied"));

        ItineraryDay day = itinerary.getDays().stream()
                .filter(d -> d.getId().equals(dayId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Day not found in itinerary"));

        return buildMapData(itinerary, day);
    }

    /**
     * Build map data structure
     */
    private MapDataDTO buildMapData(Itinerary itinerary, ItineraryDay filterDay) {
        List<MapLocationDTO> allLocations = extractLocations(itinerary, filterDay);
        Map<Integer, String> dayColors = generateDayColors(itinerary);

        // Generate GeoJSON features for all locations
        List<GeoJsonUtil.GeoJsonFeature> features = generateLocationFeatures(allLocations, dayColors);
        
        // Generate route geometry
        Map<String, Object> routeGeometry = generateRouteGeometry(allLocations);

        // Calculate map bounds
        MapBounds bounds = calculateMapBounds(allLocations);

        return MapDataDTO.builder()
                .itineraryId(itinerary.getId())
                .title(itinerary.getTitle())
                .startDate(itinerary.getStartDate())
                .endDate(itinerary.getEndDate())
                .geoJsonFeatures(GeoJsonUtil.createFeatureCollection(features))
                .routeGeometry(routeGeometry)
                .locations(allLocations)
                .dayColors(dayColors)
                .centerLatitude(bounds.centerLat)
                .centerLongitude(bounds.centerLon)
                .zoomLevel(calculateZoomLevel(bounds))
                .build();
    }

    /**
     * Extract locations from itinerary, optionally filtered by day
     */
    private List<MapLocationDTO> extractLocations(Itinerary itinerary, ItineraryDay filterDay) {
        List<MapLocationDTO> locations = new ArrayList<>();
        int sequenceNumber = 1;

        for (ItineraryDay day : itinerary.getDays()) {
            if (filterDay != null && !day.getId().equals(filterDay.getId())) {
                continue;
            }

            List<ItineraryActivity> activities = day.getActivities().stream()
                    .sorted(Comparator.comparing((ItineraryActivity a) -> a.getSortOrder() != null ? a.getSortOrder() : 0)
                            .thenComparing(a -> a.getStartTime() != null ? a.getStartTime() : java.time.LocalTime.MIDNIGHT))
                    .collect(Collectors.toList());

            int daySequence = 1;
            for (ItineraryActivity activity : activities) {
                if (activity.getLocation() != null && !activity.getLocation().isEmpty()) {
                    MapLocationDTO location = MapLocationDTO.builder()
                            .activityId(activity.getId())
                            .title(activity.getTitle())
                            .location(activity.getLocation())
                            .latitude(extractLatitude(activity.getLocation()))
                            .longitude(extractLongitude(activity.getLocation()))
                            .dayNumber(day.getDayNumber())
                            .sequenceNumber(daySequence++)
                            .activityType(activity.getActivityType().toString())
                            .timeRange(formatTimeRange(activity))
                            .description(activity.getDescription())
                            .build();

                    locations.add(location);
                    sequenceNumber++;
                }
            }
        }

        return locations;
    }

    /**
     * Generate GeoJSON features for locations
     */
    private List<GeoJsonUtil.GeoJsonFeature> generateLocationFeatures(
            List<MapLocationDTO> locations, Map<Integer, String> dayColors) {

        return locations.stream()
                .map(location -> {
                    Map<String, Object> properties = new LinkedHashMap<>();
                    properties.put("title", location.getTitle());
                    properties.put("location", location.getLocation());
                    properties.put("dayNumber", location.getDayNumber());
                    properties.put("sequenceNumber", location.getSequenceNumber());
                    properties.put("activityType", location.getActivityType());
                    properties.put("timeRange", location.getTimeRange());
                    properties.put("description", location.getDescription());
                    properties.put("color", dayColors.getOrDefault(location.getDayNumber(), "#808080"));
                    properties.put("marker-color", dayColors.getOrDefault(location.getDayNumber(), "#808080"));
                    properties.put("marker-symbol", getMarkerSymbol(location.getActivityType()));

                    return GeoJsonUtil.createPointFeature(
                            location.getLatitude().doubleValue(),
                            location.getLongitude().doubleValue(),
                            properties
                    );
                })
                .collect(Collectors.toList());
    }

    /**
     * Generate route geometry connecting all locations in order
     */
    private Map<String, Object> generateRouteGeometry(List<MapLocationDTO> locations) {
        if (locations.isEmpty()) {
            return new HashMap<>();
        }

        List<double[]> coordinates = locations.stream()
                .map(loc -> new double[]{loc.getLongitude().doubleValue(), loc.getLatitude().doubleValue()})
                .collect(Collectors.toList());

        Map<String, Object> geometry = new LinkedHashMap<>();
        geometry.put("type", "LineString");
        geometry.put("coordinates", coordinates);
        return geometry;
    }

    /**
     * Calculate map bounds from locations
     */
    private MapBounds calculateMapBounds(List<MapLocationDTO> locations) {
        if (locations.isEmpty()) {
            return new MapBounds(0, 0, 12);
        }

        double minLat = locations.stream().mapToDouble(l -> l.getLatitude().doubleValue()).min().orElse(0);
        double maxLat = locations.stream().mapToDouble(l -> l.getLatitude().doubleValue()).max().orElse(0);
        double minLon = locations.stream().mapToDouble(l -> l.getLongitude().doubleValue()).min().orElse(0);
        double maxLon = locations.stream().mapToDouble(l -> l.getLongitude().doubleValue()).max().orElse(0);

        double centerLat = (minLat + maxLat) / 2;
        double centerLon = (minLon + maxLon) / 2;

        return new MapBounds(centerLat, centerLon, 12);
    }

    /**
     * Calculate appropriate zoom level based on bounds
     */
    private Integer calculateZoomLevel(MapBounds bounds) {
        // Simple zoom calculation based on typical map bounds
        // More sophisticated algorithms could be implemented
        return 12;
    }

    /**
     * Generate color mapping for days
     */
    private Map<Integer, String> generateDayColors(Itinerary itinerary) {
        Map<Integer, String> colors = new HashMap<>();
        long daysCount = ChronoUnit.DAYS.between(itinerary.getStartDate(), itinerary.getEndDate()) + 1;

        for (int i = 1; i <= daysCount && i <= DAY_COLORS.length; i++) {
            colors.put(i, DAY_COLORS[(i - 1) % DAY_COLORS.length]);
        }

        return colors;
    }

    /**
     * Format time range for activity
     */
    private String formatTimeRange(ItineraryActivity activity) {
        if (activity.getStartTime() == null) {
            return "All day";
        }

        if (activity.getEndTime() == null) {
            return activity.getStartTime().toString();
        }

        return String.format("%s - %s", activity.getStartTime(), activity.getEndTime());
    }

    /**
     * Get marker symbol based on activity type
     */
    private String getMarkerSymbol(String activityType) {
        return switch (activityType.toUpperCase()) {
            case "ACCOMMODATION" -> "lodging";
            case "TRANSPORT" -> "car";
            case "ACTIVITY" -> "star";
            case "GUIDE" -> "tour";
            default -> "circle";
        };
    }

    /**
     * Extract latitude from location string (placeholder - would use geocoding in production)
     */
    private java.math.BigDecimal extractLatitude(String location) {
        // Placeholder: In production, use geocoding service
        // For now, return a default value
        return java.math.BigDecimal.valueOf(35.6762);
    }

    /**
     * Extract longitude from location string (placeholder - would use geocoding in production)
     */
    private java.math.BigDecimal extractLongitude(String location) {
        // Placeholder: In production, use geocoding service
        // For now, return a default value
        return java.math.BigDecimal.valueOf(139.6503);
    }

    /**
     * Helper class for map bounds
     */
    @lombok.Value
    private static class MapBounds {
        double centerLat;
        double centerLon;
        int zoom;
    }
}
