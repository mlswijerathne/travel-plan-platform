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
 *
 * When {@code google.maps.use-dummy=true} (active in dev profile) all methods
 * return realistic pre-seeded Sri Lanka data so the end-to-end AI agent workflow
 * can be tested without a live Google Maps API key.
 */
@Slf4j
@Service
public class GoogleMapsService {

    private final WebClient webClient;
    private final String apiKey;
    private final GoogleMapsConfig config;

    // ── Sri Lanka city coordinates ───────────────────────────────────────────
    private static final Map<String, double[]> CITY_COORDS = new LinkedHashMap<>();
    static {
        CITY_COORDS.put("colombo",       new double[]{6.9271,  79.8612});
        CITY_COORDS.put("kandy",         new double[]{7.2906,  80.6337});
        CITY_COORDS.put("galle",         new double[]{6.0174,  80.2222});
        CITY_COORDS.put("ella",          new double[]{6.8667,  81.0466});
        CITY_COORDS.put("sigiriya",      new double[]{7.9569,  80.7603});
        CITY_COORDS.put("nuwara eliya",  new double[]{6.9497,  80.7891});
        CITY_COORDS.put("yala",          new double[]{6.3728,  81.3789});
        CITY_COORDS.put("anuradhapura",  new double[]{8.3114,  80.4037});
        CITY_COORDS.put("polonnaruwa",   new double[]{7.9403,  81.0188});
        CITY_COORDS.put("tangalle",      new double[]{6.0267,  80.7958});
        CITY_COORDS.put("trincomalee",   new double[]{8.5922,  81.2152});
        CITY_COORDS.put("mirissa",       new double[]{5.9484,  80.4716});
        CITY_COORDS.put("negombo",       new double[]{7.2081,  79.8358});
        CITY_COORDS.put("dambulla",      new double[]{7.8675,  80.6517});
    }

    // ── Known route data (key = "origin_keyword→destination_keyword") ────────
    // Each array: [distanceKm, durationMinutes, highwayName]
    private static final Map<String, Object[]> KNOWN_ROUTES = new LinkedHashMap<>();
    static {
        KNOWN_ROUTES.put("colombo→kandy",         new Object[]{114, 195, "A1"});
        KNOWN_ROUTES.put("kandy→colombo",         new Object[]{114, 195, "A1"});
        KNOWN_ROUTES.put("colombo→galle",         new Object[]{127, 150, "Southern Expressway"});
        KNOWN_ROUTES.put("galle→colombo",         new Object[]{127, 150, "Southern Expressway"});
        KNOWN_ROUTES.put("colombo→negombo",       new Object[]{37,  60,  "B27"});
        KNOWN_ROUTES.put("kandy→ella",            new Object[]{168, 300, "B49/A16"});
        KNOWN_ROUTES.put("ella→kandy",            new Object[]{168, 300, "A16/B49"});
        KNOWN_ROUTES.put("ella→yala",             new Object[]{90,  150, "B402"});
        KNOWN_ROUTES.put("yala→ella",             new Object[]{90,  150, "B402"});
        KNOWN_ROUTES.put("kandy→nuwara eliya",    new Object[]{75,  165, "A5"});
        KNOWN_ROUTES.put("nuwara eliya→ella",     new Object[]{82,  180, "A16"});
        KNOWN_ROUTES.put("galle→tangalle",        new Object[]{68,  90,  "A2"});
        KNOWN_ROUTES.put("tangalle→yala",         new Object[]{60,  90,  "A2"});
        KNOWN_ROUTES.put("sigiriya→polonnaruwa",  new Object[]{55,  90,  "B238"});
        KNOWN_ROUTES.put("dambulla→sigiriya",     new Object[]{19,  30,  "B238"});
        KNOWN_ROUTES.put("colombo→sigiriya",      new Object[]{175, 240, "A9"});
        KNOWN_ROUTES.put("colombo→anuradhapura",  new Object[]{205, 270, "A9"});
        KNOWN_ROUTES.put("galle→mirissa",         new Object[]{32,  45,  "A2"});
    }

    public GoogleMapsService(WebClient googleMapsWebClient, GoogleMapsConfig config) {
        this.webClient = googleMapsWebClient;
        this.apiKey = config.getApiKey();
        this.config = config;
    }

    // ──────────────────────────────────────────────
    //  Directions API
    // ──────────────────────────────────────────────

    /**
     * Get directions between two places.
     * Returns route summary, distance, duration, and step-by-step instructions.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getDirections(String origin, String destination, String mode) {
        log.info("Google Maps Directions: {} → {} [{}]", origin, destination, mode);

        if (config.isUseDummy()) {
            log.info("[DUMMY] Returning dummy directions: {} → {}", origin, destination);
            return getDummyDirections(origin, destination);
        }

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
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> searchNearbyPlaces(double latitude, double longitude,
                                                   int radius, String type) {
        log.info("Google Maps Nearby Search: ({}, {}) radius={} type={}", latitude, longitude, radius, type);

        if (config.isUseDummy()) {
            log.info("[DUMMY] Returning dummy nearby places: ({}, {}) type={}", latitude, longitude, type);
            return getDummyNearbyPlaces(latitude, longitude, type);
        }

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
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> geocode(String address) {
        log.info("Google Maps Geocoding: {}", address);

        if (config.isUseDummy()) {
            log.info("[DUMMY] Returning dummy geocode for: {}", address);
            return getDummyGeocode(address);
        }

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
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getDistanceMatrix(String origins, String destinations, String mode) {
        log.info("Google Maps Distance Matrix: {} → {} [{}]", origins, destinations, mode);

        if (config.isUseDummy()) {
            log.info("[DUMMY] Returning dummy distance matrix: {} → {}", origins, destinations);
            return getDummyDistanceMatrix(origins, destinations);
        }

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
    //  Dummy Data Methods (dev profile)
    // ──────────────────────────────────────────────

    private Map<String, Object> getDummyDirections(String origin, String destination) {
        String key = buildRouteKey(origin, destination);
        Object[] route = KNOWN_ROUTES.getOrDefault(key, new Object[]{100, 180, "A1"});

        int distKm      = (int) route[0];
        int durationMin = (int) route[1];
        String highway  = (String) route[2];

        String originCity = extractCity(origin);
        String destCity   = extractCity(destination);

        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        result.put("source", "Dummy Data (dev mode)");
        result.put("distance", distKm + " km");
        result.put("distanceMeters", distKm * 1000);
        result.put("duration", formatDuration(durationMin));
        result.put("durationSeconds", durationMin * 60);
        result.put("startAddress", originCity + ", Sri Lanka");
        result.put("endAddress", destCity + ", Sri Lanka");
        result.put("summary", highway);

        List<Map<String, String>> steps = new ArrayList<>();
        Map<String, String> step1 = new HashMap<>();
        step1.put("instruction", "Head south on " + highway);
        step1.put("distance", distKm / 3 + " km");
        step1.put("duration", formatDuration(durationMin / 3));
        steps.add(step1);

        Map<String, String> step2 = new HashMap<>();
        step2.put("instruction", "Continue on main highway towards " + destCity);
        step2.put("distance", distKm * 2 / 3 + " km");
        step2.put("duration", formatDuration(durationMin * 2 / 3));
        steps.add(step2);

        result.put("steps", steps);
        return result;
    }

    private Map<String, Object> getDummyGeocode(String address) {
        String key = address.toLowerCase();
        double[] coords = new double[]{6.9271, 79.8612}; // default Colombo
        String formattedAddress = address + ", Sri Lanka";

        for (Map.Entry<String, double[]> entry : CITY_COORDS.entrySet()) {
            if (key.contains(entry.getKey())) {
                coords = entry.getValue();
                String city = entry.getKey().substring(0, 1).toUpperCase() + entry.getKey().substring(1);
                formattedAddress = city + ", Sri Lanka";
                break;
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        result.put("source", "Dummy Data (dev mode)");
        result.put("formattedAddress", formattedAddress);
        result.put("latitude", coords[0]);
        result.put("longitude", coords[1]);
        result.put("placeId", "ChIJDummy_" + address.replaceAll("\\s+", "_"));
        return result;
    }

    private Map<String, Object> getDummyNearbyPlaces(double latitude, double longitude, String type) {
        List<Map<String, Object>> places = new ArrayList<>();

        String nearestCity = findNearestCity(latitude, longitude);

        if ("lodging".equals(type) || "hotel".equals(type)) {
            places.add(dummyPlace("Grand " + capitalize(nearestCity) + " Hotel",
                    "Main Street, " + capitalize(nearestCity), 4.2, 187,
                    latitude + 0.005, longitude + 0.005, List.of("lodging")));
            places.add(dummyPlace(capitalize(nearestCity) + " Heritage Inn",
                    "Lake Road, " + capitalize(nearestCity), 4.0, 93,
                    latitude - 0.003, longitude + 0.008, List.of("lodging", "spa")));
            places.add(dummyPlace("Sunset Guest House " + capitalize(nearestCity),
                    "Beach Lane, " + capitalize(nearestCity), 3.8, 52,
                    latitude + 0.008, longitude - 0.004, List.of("lodging")));
        } else if ("restaurant".equals(type)) {
            places.add(dummyPlace("Spice Garden Restaurant",
                    "Fort Road, " + capitalize(nearestCity), 4.5, 312,
                    latitude + 0.002, longitude + 0.003, List.of("restaurant", "food")));
            places.add(dummyPlace("Ceylon Kitchen",
                    "Temple Street, " + capitalize(nearestCity), 4.3, 156,
                    latitude - 0.004, longitude + 0.001, List.of("restaurant")));
            places.add(dummyPlace("Taste of Lanka",
                    "Harbour View, " + capitalize(nearestCity), 4.1, 78,
                    latitude + 0.006, longitude - 0.002, List.of("restaurant", "bar")));
        } else if ("tourist_attraction".equals(type)) {
            places.add(dummyPlace(capitalize(nearestCity) + " Ancient Fort",
                    capitalize(nearestCity) + " District", 4.6, 2341,
                    latitude + 0.01, longitude + 0.01, List.of("tourist_attraction", "museum")));
            places.add(dummyPlace("Royal Botanical Gardens",
                    "Garden Road, " + capitalize(nearestCity), 4.4, 1876,
                    latitude - 0.01, longitude + 0.007, List.of("tourist_attraction", "park")));
            places.add(dummyPlace("Sri Lanka Cultural Museum",
                    "Heritage Lane, " + capitalize(nearestCity), 4.3, 987,
                    latitude + 0.007, longitude - 0.012, List.of("tourist_attraction")));
        } else if ("car_rental".equals(type)) {
            places.add(dummyPlace("Lanka Car Rentals",
                    "Station Road, " + capitalize(nearestCity), 4.1, 45,
                    latitude + 0.003, longitude + 0.004, List.of("car_rental")));
            places.add(dummyPlace("Island Drive Auto",
                    "Main Junction, " + capitalize(nearestCity), 3.9, 28,
                    latitude - 0.005, longitude + 0.002, List.of("car_rental")));
        } else {
            places.add(dummyPlace(capitalize(nearestCity) + " Point of Interest",
                    "Central Area, " + capitalize(nearestCity), 4.0, 100,
                    latitude, longitude, List.of(type)));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        result.put("source", "Dummy Data (dev mode)");
        result.put("type", type);
        result.put("count", places.size());
        result.put("places", places);
        return result;
    }

    private Map<String, Object> getDummyDistanceMatrix(String origins, String destinations) {
        String[] originList = origins.split("\\|");
        String[] destList   = destinations.split("\\|");

        List<Map<String, Object>> matrix = new ArrayList<>();
        for (String origin : originList) {
            for (String dest : destList) {
                String routeKey = buildRouteKey(origin.trim(), dest.trim());
                Object[] route = KNOWN_ROUTES.getOrDefault(routeKey, new Object[]{100, 180, "A1"});
                int distKm      = (int) route[0];
                int durationMin = (int) route[1];

                Map<String, Object> entry = new HashMap<>();
                entry.put("origin", origin.trim() + ", Sri Lanka");
                entry.put("destination", dest.trim() + ", Sri Lanka");
                entry.put("distance", distKm + " km");
                entry.put("distanceMeters", distKm * 1000);
                entry.put("duration", formatDuration(durationMin));
                entry.put("durationSeconds", durationMin * 60);
                matrix.add(entry);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        result.put("source", "Dummy Data (dev mode)");
        result.put("matrix", matrix);
        return result;
    }

    // ──────────────────────────────────────────────
    //  Dummy Data Helpers
    // ──────────────────────────────────────────────

    private String buildRouteKey(String origin, String destination) {
        return extractCity(origin) + "→" + extractCity(destination);
    }

    private String extractCity(String location) {
        if (location == null) return "colombo";
        String lower = location.toLowerCase()
                .replace(", sri lanka", "")
                .replace(",sri lanka", "")
                .trim();
        for (String city : CITY_COORDS.keySet()) {
            if (lower.contains(city)) return city;
        }
        return lower;
    }

    private String findNearestCity(double lat, double lng) {
        String nearest = "colombo";
        double minDist = Double.MAX_VALUE;
        for (Map.Entry<String, double[]> entry : CITY_COORDS.entrySet()) {
            double[] c = entry.getValue();
            double dist = Math.abs(c[0] - lat) + Math.abs(c[1] - lng);
            if (dist < minDist) {
                minDist = dist;
                nearest = entry.getKey();
            }
        }
        return nearest;
    }

    private String capitalize(String word) {
        if (word == null || word.isEmpty()) return word;
        return Character.toUpperCase(word.charAt(0)) + word.substring(1);
    }

    private String formatDuration(int minutes) {
        if (minutes < 60) return minutes + " mins";
        int h = minutes / 60;
        int m = minutes % 60;
        return m == 0 ? h + " hour" + (h > 1 ? "s" : "") : h + " hour" + (h > 1 ? "s" : "") + " " + m + " mins";
    }

    private Map<String, Object> dummyPlace(String name, String address, double rating,
                                            int totalRatings, double lat, double lng,
                                            List<String> types) {
        Map<String, Object> p = new HashMap<>();
        p.put("name", name);
        p.put("address", address);
        p.put("rating", rating);
        p.put("totalRatings", totalRatings);
        p.put("placeId", "ChIJDummy_" + name.replaceAll("\\s+", "_"));
        p.put("priceLevel", 2);
        p.put("openNow", true);
        p.put("types", types);
        p.put("source", "Dummy Data (dev mode)");
        p.put("latitude", lat);
        p.put("longitude", lng);
        return p;
    }

    // ──────────────────────────────────────────────
    //  Response Parsers (live API mode)
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
                Map<String, Object> stepDur  = (Map<String, Object>) step.get("duration");
                if (stepDist != null) s.put("distance", (String) stepDist.get("text"));
                if (stepDur  != null) s.put("duration", (String) stepDur.get("text"));
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

        Map<String, Object> first    = results.get(0);
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
        List<String> destAddresses   = (List<String>) raw.get("destination_addresses");
        List<Map<String, Object>> rows = (List<Map<String, Object>>) raw.get("rows");

        List<Map<String, Object>> matrix = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            List<Map<String, Object>> elements = (List<Map<String, Object>>) rows.get(i).get("elements");
            for (int j = 0; j < elements.size(); j++) {
                Map<String, Object> element      = elements.get(j);
                String              elementStatus = (String) element.get("status");

                Map<String, Object> entry = new HashMap<>();
                entry.put("origin",      originAddresses.get(i));
                entry.put("destination", destAddresses.get(j));

                if ("OK".equals(elementStatus)) {
                    Map<String, Object> dist = (Map<String, Object>) element.get("distance");
                    Map<String, Object> dur  = (Map<String, Object>) element.get("duration");
                    entry.put("distance",        dist.get("text"));
                    entry.put("distanceMeters",  dist.get("value"));
                    entry.put("duration",        dur.get("text"));
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
