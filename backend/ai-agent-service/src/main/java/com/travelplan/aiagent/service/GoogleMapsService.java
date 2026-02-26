package com.travelplan.aiagent.service;

import com.travelplan.aiagent.config.GoogleMapsConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

/**
 * Wraps Google Maps Platform REST APIs:
 * - Directions API  (routes, travel times, step-by-step)
 * - Places Nearby Search (hotels, restaurants, attractions near a point)
 * - Geocoding API   (place name → lat/lng)
 * - Distance Matrix  (multi-origin/destination travel times)
 */
@Slf4j
@Service
public class GoogleMapsService {

    private final WebClient webClient;
    private final String apiKey;

    public GoogleMapsService(WebClient googleMapsWebClient, GoogleMapsConfig config) {
        this.webClient = googleMapsWebClient;
        this.apiKey = config.getApiKey();
    }

    // ──────────────────────────────────────────────
    //  Directions API
    // ──────────────────────────────────────────────

    /**
     * Get directions between two places.
     * Returns route summary, distance, duration, and step-by-step instructions.
     *
     * @param origin      e.g. "Colombo, Sri Lanka"
     * @param destination e.g. "Kandy, Sri Lanka"
     * @param mode        driving | transit | walking | bicycling (default: driving)
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getDirections(String origin, String destination, String mode) {
        log.info("Google Maps Directions: {} → {} [{}]", origin, destination, mode);

        try {
            Map<String, Object> raw = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/directions/json")
                            .queryParam("origin", origin)
                            .queryParam("destination", destination)
                            .queryParam("mode", mode != null ? mode : "driving")
                            .queryParam("region", "lk")
                            .queryParam("key", apiKey)
                            .build())
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            return parseDirectionsResponse(raw);
        } catch (Exception e) {
            log.error("Directions API error: {}", e.getMessage());
            return errorResult("Directions API unavailable: " + e.getMessage());
        }
    }

    // ──────────────────────────────────────────────
    //  Places Nearby Search
    // ──────────────────────────────────────────────

    /**
     * Search for places near a location.
     *
     * @param latitude  latitude of the center point
     * @param longitude longitude of the center point
     * @param radius    search radius in meters (max 50000)
     * @param type      Google place type: lodging | restaurant | tourist_attraction | car_rental
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> searchNearbyPlaces(double latitude, double longitude,
                                                   int radius, String type) {
        log.info("Google Maps Nearby Search: ({}, {}) radius={} type={}", latitude, longitude, radius, type);

        try {
            Map<String, Object> raw = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/place/nearbysearch/json")
                            .queryParam("location", latitude + "," + longitude)
                            .queryParam("radius", radius)
                            .queryParam("type", type)
                            .queryParam("key", apiKey)
                            .build())
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            return parsePlacesResponse(raw, type);
        } catch (Exception e) {
            log.error("Places Nearby Search error: {}", e.getMessage());
            return errorResult("Places API unavailable: " + e.getMessage());
        }
    }

    // ──────────────────────────────────────────────
    //  Geocoding API
    // ──────────────────────────────────────────────

    /**
     * Convert a place name to lat/lng coordinates.
     *
     * @param address e.g. "Sigiriya, Sri Lanka"
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> geocode(String address) {
        log.info("Google Maps Geocoding: {}", address);

        try {
            Map<String, Object> raw = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/geocode/json")
                            .queryParam("address", address)
                            .queryParam("region", "lk")
                            .queryParam("key", apiKey)
                            .build())
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            return parseGeocodeResponse(raw);
        } catch (Exception e) {
            log.error("Geocoding API error: {}", e.getMessage());
            return errorResult("Geocoding API unavailable: " + e.getMessage());
        }
    }

    // ──────────────────────────────────────────────
    //  Distance Matrix API
    // ──────────────────────────────────────────────

    /**
     * Get travel time and distance between multiple origins and destinations.
     *
     * @param origins      pipe-separated list, e.g. "Colombo|Kandy"
     * @param destinations pipe-separated list, e.g. "Ella|Galle"
     * @param mode         driving | transit | walking | bicycling
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getDistanceMatrix(String origins, String destinations, String mode) {
        log.info("Google Maps Distance Matrix: {} → {} [{}]", origins, destinations, mode);

        try {
            Map<String, Object> raw = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/distancematrix/json")
                            .queryParam("origins", origins)
                            .queryParam("destinations", destinations)
                            .queryParam("mode", mode != null ? mode : "driving")
                            .queryParam("region", "lk")
                            .queryParam("key", apiKey)
                            .build())
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            return parseDistanceMatrixResponse(raw);
        } catch (Exception e) {
            log.error("Distance Matrix API error: {}", e.getMessage());
            return errorResult("Distance Matrix API unavailable: " + e.getMessage());
        }
    }

    // ──────────────────────────────────────────────
    //  Response Parsers
    // ──────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseDirectionsResponse(Map<String, Object> raw) {
        Map<String, Object> result = new HashMap<>();

        String status = (String) raw.getOrDefault("status", "UNKNOWN");
        if (!"OK".equals(status)) {
            result.put("status", "error");
            result.put("message", "Directions API returned status: " + status);
            return result;
        }

        List<Map<String, Object>> routes = (List<Map<String, Object>>) raw.get("routes");
        if (routes == null || routes.isEmpty()) {
            result.put("status", "no_results");
            result.put("message", "No route found between the given locations.");
            return result;
        }

        Map<String, Object> route = routes.get(0);
        List<Map<String, Object>> legs = (List<Map<String, Object>>) route.get("legs");
        Map<String, Object> leg = legs.get(0);

        Map<String, Object> distance = (Map<String, Object>) leg.get("distance");
        Map<String, Object> duration = (Map<String, Object>) leg.get("duration");

        result.put("status", "success");
        result.put("source", "Google Maps");
        result.put("distance", distance.get("text"));
        result.put("distanceMeters", distance.get("value"));
        result.put("duration", duration.get("text"));
        result.put("durationSeconds", duration.get("value"));
        result.put("startAddress", leg.get("start_address"));
        result.put("endAddress", leg.get("end_address"));
        result.put("summary", route.get("summary"));

        // Extract step-by-step directions
        List<Map<String, Object>> steps = (List<Map<String, Object>>) leg.get("steps");
        List<Map<String, String>> simpleSteps = new ArrayList<>();
        if (steps != null) {
            for (Map<String, Object> step : steps) {
                Map<String, String> s = new HashMap<>();
                String instruction = (String) step.get("html_instructions");
                if (instruction != null) {
                    s.put("instruction", instruction.replaceAll("<[^>]*>", ""));
                }
                Map<String, Object> stepDist = (Map<String, Object>) step.get("distance");
                Map<String, Object> stepDur = (Map<String, Object>) step.get("duration");
                if (stepDist != null) s.put("distance", (String) stepDist.get("text"));
                if (stepDur != null) s.put("duration", (String) stepDur.get("text"));
                simpleSteps.add(s);
            }
        }
        result.put("steps", simpleSteps);

        return result;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parsePlacesResponse(Map<String, Object> raw, String type) {
        Map<String, Object> result = new HashMap<>();

        String status = (String) raw.getOrDefault("status", "UNKNOWN");
        if (!"OK".equals(status) && !"ZERO_RESULTS".equals(status)) {
            result.put("status", "error");
            result.put("message", "Places API returned status: " + status);
            return result;
        }

        List<Map<String, Object>> results = (List<Map<String, Object>>) raw.getOrDefault("results", List.of());

        List<Map<String, Object>> places = new ArrayList<>();
        for (Map<String, Object> place : results) {
            Map<String, Object> p = new HashMap<>();
            p.put("name", place.get("name"));
            p.put("address", place.get("vicinity"));
            p.put("rating", place.get("rating"));
            p.put("totalRatings", place.get("user_ratings_total"));
            p.put("placeId", place.get("place_id"));
            p.put("priceLevel", place.get("price_level"));
            p.put("openNow", extractOpenNow(place));
            p.put("types", place.get("types"));
            p.put("source", "Google Maps");

            Map<String, Object> geometry = (Map<String, Object>) place.get("geometry");
            if (geometry != null) {
                Map<String, Object> location = (Map<String, Object>) geometry.get("location");
                if (location != null) {
                    p.put("latitude", location.get("lat"));
                    p.put("longitude", location.get("lng"));
                }
            }

            places.add(p);
        }

        result.put("status", "success");
        result.put("source", "Google Maps");
        result.put("type", type);
        result.put("count", places.size());
        result.put("places", places);
        return result;
    }

    @SuppressWarnings("unchecked")
    private Boolean extractOpenNow(Map<String, Object> place) {
        Map<String, Object> openingHours = (Map<String, Object>) place.get("opening_hours");
        if (openingHours != null) {
            return (Boolean) openingHours.get("open_now");
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseGeocodeResponse(Map<String, Object> raw) {
        Map<String, Object> result = new HashMap<>();

        String status = (String) raw.getOrDefault("status", "UNKNOWN");
        if (!"OK".equals(status)) {
            result.put("status", "error");
            result.put("message", "Geocoding API returned status: " + status);
            return result;
        }

        List<Map<String, Object>> results = (List<Map<String, Object>>) raw.get("results");
        if (results == null || results.isEmpty()) {
            result.put("status", "no_results");
            result.put("message", "Could not geocode the given address.");
            return result;
        }

        Map<String, Object> first = results.get(0);
        Map<String, Object> geometry = (Map<String, Object>) first.get("geometry");
        Map<String, Object> location = (Map<String, Object>) geometry.get("location");

        result.put("status", "success");
        result.put("source", "Google Maps");
        result.put("formattedAddress", first.get("formatted_address"));
        result.put("latitude", location.get("lat"));
        result.put("longitude", location.get("lng"));
        result.put("placeId", first.get("place_id"));

        return result;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseDistanceMatrixResponse(Map<String, Object> raw) {
        Map<String, Object> result = new HashMap<>();

        String status = (String) raw.getOrDefault("status", "UNKNOWN");
        if (!"OK".equals(status)) {
            result.put("status", "error");
            result.put("message", "Distance Matrix API returned status: " + status);
            return result;
        }

        List<String> originAddresses = (List<String>) raw.get("origin_addresses");
        List<String> destAddresses = (List<String>) raw.get("destination_addresses");
        List<Map<String, Object>> rows = (List<Map<String, Object>>) raw.get("rows");

        List<Map<String, Object>> matrix = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            List<Map<String, Object>> elements = (List<Map<String, Object>>) rows.get(i).get("elements");
            for (int j = 0; j < elements.size(); j++) {
                Map<String, Object> element = elements.get(j);
                String elementStatus = (String) element.get("status");

                Map<String, Object> entry = new HashMap<>();
                entry.put("origin", originAddresses.get(i));
                entry.put("destination", destAddresses.get(j));

                if ("OK".equals(elementStatus)) {
                    Map<String, Object> dist = (Map<String, Object>) element.get("distance");
                    Map<String, Object> dur = (Map<String, Object>) element.get("duration");
                    entry.put("distance", dist.get("text"));
                    entry.put("distanceMeters", dist.get("value"));
                    entry.put("duration", dur.get("text"));
                    entry.put("durationSeconds", dur.get("value"));
                } else {
                    entry.put("error", "No route: " + elementStatus);
                }

                matrix.add(entry);
            }
        }

        result.put("status", "success");
        result.put("source", "Google Maps");
        result.put("matrix", matrix);
        return result;
    }

    private Map<String, Object> errorResult(String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "error");
        result.put("message", message);
        return result;
    }
}
