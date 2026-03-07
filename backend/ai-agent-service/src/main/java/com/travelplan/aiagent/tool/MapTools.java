package com.travelplan.aiagent.tool;

import com.google.adk.tools.Annotations.Schema;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * OpenStreetMap ADK tools.
 * Provides real-time travel data: directions, distances, nearby places, and geocoding.
 * These tools supplement platform-registered provider data — platform partners always take priority.
 */
@Slf4j
public class MapTools {

    @Schema(description = "Get driving directions between two locations in Sri Lanka. Returns real distance, travel time, route summary, and step-by-step directions.")
    public static Map<String, Object> getDirections(
            @Schema(description = "Starting location, e.g. 'Colombo, Sri Lanka' or 'Bandaranaike Airport'") String origin,
            @Schema(description = "Destination, e.g. 'Kandy, Sri Lanka' or 'Sigiriya'") String destination,
            @Schema(description = "Travel mode: 'driving' (default), 'walking', or 'bicycling'") String mode) {

        String effectiveMode = (mode != null && !mode.isBlank()) ? mode : "driving";
        log.info("ADK Tool getDirections: {} → {} [{}]", origin, destination, effectiveMode);
        return ToolRegistry.getInstance().getOpenStreetMapService()
                .getDirections(origin, destination, effectiveMode);
    }

    @Schema(description = "Search for places near a location using OpenStreetMap. Use this to find hotels, restaurants, attractions, or car rentals near a specific point. NOTE: Always check platform-registered providers FIRST before using this tool.")
    public static Map<String, Object> searchNearbyPlaces(
            @Schema(description = "Latitude of the center point") double latitude,
            @Schema(description = "Longitude of the center point") double longitude,
            @Schema(description = "Search radius in meters, e.g. 5000 for 5km") int radius,
            @Schema(description = "Type of place to search: 'lodging' (hotels), 'restaurant', 'tourist_attraction', 'car_rental'") String type) {

        log.info("ADK Tool searchNearbyPlaces: ({}, {}) r={} type={}", latitude, longitude, radius, type);
        return ToolRegistry.getInstance().getOpenStreetMapService()
                .searchNearbyPlaces(latitude, longitude, radius, type);
    }

    @Schema(description = "Get the latitude and longitude coordinates for a place name in Sri Lanka. Useful for finding exact locations before doing nearby searches or calculating distances.")
    public static Map<String, Object> geocodeLocation(
            @Schema(description = "Place name or address to geocode, e.g. 'Sigiriya, Sri Lanka' or 'Temple of the Tooth, Kandy'") String address) {

        log.info("ADK Tool geocodeLocation: {}", address);
        return ToolRegistry.getInstance().getOpenStreetMapService()
                .geocode(address);
    }

    @Schema(description = "Get travel times and distances between multiple origins and destinations at once. Use this to plan multi-stop itineraries with accurate travel times.")
    public static Map<String, Object> getDistanceMatrix(
            @Schema(description = "Pipe-separated origin locations, e.g. 'Colombo, Sri Lanka|Kandy, Sri Lanka'") String origins,
            @Schema(description = "Pipe-separated destination locations, e.g. 'Ella, Sri Lanka|Galle, Sri Lanka'") String destinations,
            @Schema(description = "Travel mode: 'driving' (default), 'walking'") String mode) {

        String effectiveMode = (mode != null && !mode.isBlank()) ? mode : "driving";
        log.info("ADK Tool getDistanceMatrix: {} → {} [{}]", origins, destinations, effectiveMode);
        return ToolRegistry.getInstance().getOpenStreetMapService()
                .getDistanceMatrix(origins, destinations, effectiveMode);
    }
}
