package com.travelplan.aiagent.service;

import com.travelplan.aiagent.config.OpenStreetMapConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

/**
 * Wraps OpenStreetMap ecosystem REST APIs:
 * - Nominatim       (place name → lat/lng geocoding)
 * - OSRM Route      (directions, travel times, step-by-step)
 * - Overpass API     (POI nearby search: hotels, restaurants, attractions)
 * - OSRM Table      (multi-origin/destination distance matrix)
 *
 * When {@code osm.use-dummy=true} (active in dev profile) all methods
 * return realistic pre-seeded Sri Lanka data so the end-to-end AI agent workflow
 * can be tested without live API calls.
 */
@Slf4j
@Service
public class OpenStreetMapService {

    private final WebClient nominatimClient;
    private final WebClient osrmClient;
    private final WebClient overpassClient;
    private final OpenStreetMapConfig config;

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

    public OpenStreetMapService(@Qualifier("nominatimWebClient") WebClient nominatimClient,
                                @Qualifier("osrmWebClient") WebClient osrmClient,
                                @Qualifier("overpassWebClient") WebClient overpassClient,
                                OpenStreetMapConfig config) {
        this.nominatimClient = nominatimClient;
        this.osrmClient = osrmClient;
        this.overpassClient = overpassClient;
        this.config = config;
    }

    // ──────────────────────────────────────────────
    //  Geocoding (Nominatim)
    // ──────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    public Map<String, Object> geocode(String address) {
        log.info("Nominatim Geocoding: {}", address);

        if (config.isUseDummy()) {
            log.info("[DUMMY] Returning dummy geocode for: {}", address);
            return getDummyGeocode(address);
        }

        try {
            List<Map<String, Object>> results = nominatimClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/search")
                            .queryParam("q", address)
                            .queryParam("format", "json")
                            .queryParam("countrycodes", "lk")
                            .queryParam("limit", 1)
                            .queryParam("addressdetails", 1)
                            .build())
                    .retrieve()
                    .bodyToMono(List.class)
                    .block();

            return parseNominatimGeocodeResponse(results, address);
        } catch (Exception e) {
            log.error("Nominatim Geocoding error: {}", e.getMessage());
            return errorResult("Geocoding API unavailable: " + e.getMessage());
        }
    }

    // ──────────────────────────────────────────────
    //  Directions (OSRM Route)
    // ──────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    public Map<String, Object> getDirections(String origin, String destination, String mode) {
        log.info("OSRM Directions: {} → {} [{}]", origin, destination, mode);

        if (config.isUseDummy()) {
            log.info("[DUMMY] Returning dummy directions: {} → {}", origin, destination);
            return getDummyDirections(origin, destination);
        }

        try {
            // First geocode both locations to get coordinates
            Map<String, Object> originGeo = geocode(origin);
            Map<String, Object> destGeo = geocode(destination);

            if (!"success".equals(originGeo.get("status")) || !"success".equals(destGeo.get("status"))) {
                return errorResult("Could not geocode origin or destination");
            }

            double originLat = ((Number) originGeo.get("latitude")).doubleValue();
            double originLon = ((Number) originGeo.get("longitude")).doubleValue();
            double destLat = ((Number) destGeo.get("latitude")).doubleValue();
            double destLon = ((Number) destGeo.get("longitude")).doubleValue();

            String profile = mapModeToOsrmProfile(mode);

            Map<String, Object> raw = osrmClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/route/v1/" + profile + "/"
                                    + originLon + "," + originLat + ";"
                                    + destLon + "," + destLat)
                            .queryParam("overview", "full")
                            .queryParam("steps", "true")
                            .build())
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            return parseOsrmRouteResponse(raw, origin, destination);
        } catch (Exception e) {
            log.error("OSRM Directions error: {}", e.getMessage());
            return errorResult("Directions API unavailable: " + e.getMessage());
        }
    }

    // ──────────────────────────────────────────────
    //  Nearby Places Search (Overpass API)
    // ──────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    public Map<String, Object> searchNearbyPlaces(double latitude, double longitude,
                                                   int radius, String type) {
        log.info("Overpass Nearby Search: ({}, {}) radius={} type={}", latitude, longitude, radius, type);

        if (config.isUseDummy()) {
            log.info("[DUMMY] Returning dummy nearby places: ({}, {}) type={}", latitude, longitude, type);
            return getDummyNearbyPlaces(latitude, longitude, type);
        }

        try {
            String osmFilter = mapTypeToOverpassFilter(type);

            String query = "[out:json][timeout:25];"
                    + "(" + osmFilter.replace("{radius}", String.valueOf(radius))
                                     .replace("{lat}", String.valueOf(latitude))
                                     .replace("{lon}", String.valueOf(longitude))
                    + ");"
                    + "out body 10;";

            Map<String, Object> raw = overpassClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/interpreter")
                            .queryParam("data", query)
                            .build())
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            return parseOverpassResponse(raw, type);
        } catch (Exception e) {
            log.error("Overpass Nearby Search error: {}", e.getMessage());
            return errorResult("Places API unavailable: " + e.getMessage());
        }
    }

    // ──────────────────────────────────────────────
    //  Distance Matrix (OSRM Table)
    // ──────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    public Map<String, Object> getDistanceMatrix(String origins, String destinations, String mode) {
        log.info("OSRM Distance Matrix: {} → {} [{}]", origins, destinations, mode);

        if (config.isUseDummy()) {
            log.info("[DUMMY] Returning dummy distance matrix: {} → {}", origins, destinations);
            return getDummyDistanceMatrix(origins, destinations);
        }

        try {
            String[] originList = origins.split("\\|");
            String[] destList = destinations.split("\\|");

            // Geocode all locations
            List<double[]> originCoords = new ArrayList<>();
            List<double[]> destCoords = new ArrayList<>();
            List<String> originNames = new ArrayList<>();
            List<String> destNames = new ArrayList<>();

            for (String o : originList) {
                Map<String, Object> geo = geocode(o.trim());
                if ("success".equals(geo.get("status"))) {
                    originCoords.add(new double[]{
                            ((Number) geo.get("latitude")).doubleValue(),
                            ((Number) geo.get("longitude")).doubleValue()
                    });
                    originNames.add((String) geo.getOrDefault("formattedAddress", o.trim()));
                }
            }
            for (String d : destList) {
                Map<String, Object> geo = geocode(d.trim());
                if ("success".equals(geo.get("status"))) {
                    destCoords.add(new double[]{
                            ((Number) geo.get("latitude")).doubleValue(),
                            ((Number) geo.get("longitude")).doubleValue()
                    });
                    destNames.add((String) geo.getOrDefault("formattedAddress", d.trim()));
                }
            }

            if (originCoords.isEmpty() || destCoords.isEmpty()) {
                return errorResult("Could not geocode origins or destinations");
            }

            // Build OSRM Table API coordinate string: all origins, then all destinations
            StringBuilder coordsStr = new StringBuilder();
            List<Integer> sourceIndices = new ArrayList<>();
            List<Integer> destIndices = new ArrayList<>();
            int idx = 0;

            for (double[] c : originCoords) {
                if (idx > 0) coordsStr.append(";");
                coordsStr.append(c[1]).append(",").append(c[0]); // lon,lat
                sourceIndices.add(idx++);
            }
            for (double[] c : destCoords) {
                if (idx > 0) coordsStr.append(";");
                coordsStr.append(c[1]).append(",").append(c[0]);
                destIndices.add(idx++);
            }

            String sourcesParam = String.join(";", sourceIndices.stream().map(String::valueOf).toArray(String[]::new));
            String destsParam = String.join(";", destIndices.stream().map(String::valueOf).toArray(String[]::new));

            String profile = mapModeToOsrmProfile(mode);

            Map<String, Object> raw = osrmClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/table/v1/" + profile + "/" + coordsStr)
                            .queryParam("sources", sourcesParam)
                            .queryParam("destinations", destsParam)
                            .queryParam("annotations", "distance,duration")
                            .build())
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            return parseOsrmTableResponse(raw, originNames, destNames);
        } catch (Exception e) {
            log.error("OSRM Distance Matrix error: {}", e.getMessage());
            return errorResult("Distance Matrix API unavailable: " + e.getMessage());
        }
    }

    // ──────────────────────────────────────────────
    //  Response Parsers (live API mode)
    // ──────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseNominatimGeocodeResponse(List<Map<String, Object>> results, String address) {
        Map<String, Object> result = new HashMap<>();

        if (results == null || results.isEmpty()) {
            result.put("status", "no_results");
            result.put("message", "Could not geocode the given address.");
            return result;
        }

        Map<String, Object> first = results.get(0);

        result.put("status", "success");
        result.put("source", "OpenStreetMap");
        result.put("formattedAddress", first.getOrDefault("display_name", address));
        result.put("latitude", Double.parseDouble((String) first.get("lat")));
        result.put("longitude", Double.parseDouble((String) first.get("lon")));
        result.put("placeId", String.valueOf(first.get("osm_id")));
        return result;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseOsrmRouteResponse(Map<String, Object> raw, String origin, String destination) {
        Map<String, Object> result = new HashMap<>();

        String code = (String) raw.getOrDefault("code", "Error");
        if (!"Ok".equals(code)) {
            result.put("status", "error");
            result.put("message", "OSRM returned: " + code);
            return result;
        }

        List<Map<String, Object>> routes = (List<Map<String, Object>>) raw.get("routes");
        if (routes == null || routes.isEmpty()) {
            result.put("status", "no_results");
            result.put("message", "No route found between the given locations.");
            return result;
        }

        Map<String, Object> route = routes.get(0);
        double distanceMeters = ((Number) route.get("distance")).doubleValue();
        double durationSeconds = ((Number) route.get("duration")).doubleValue();

        double distanceKm = distanceMeters / 1000.0;
        int durationMin = (int) (durationSeconds / 60.0);

        result.put("status", "success");
        result.put("source", "OpenStreetMap (OSRM)");
        result.put("distance", String.format("%.1f km", distanceKm));
        result.put("distanceMeters", (int) distanceMeters);
        result.put("duration", formatDuration(durationMin));
        result.put("durationSeconds", (int) durationSeconds);
        result.put("startAddress", origin);
        result.put("endAddress", destination);
        result.put("summary", String.format("%.0f km via road", distanceKm));

        // Parse steps from legs
        List<Map<String, Object>> legs = (List<Map<String, Object>>) route.get("legs");
        List<Map<String, String>> simpleSteps = new ArrayList<>();
        if (legs != null) {
            for (Map<String, Object> leg : legs) {
                List<Map<String, Object>> steps = (List<Map<String, Object>>) leg.get("steps");
                if (steps != null) {
                    for (Map<String, Object> step : steps) {
                        Map<String, Object> maneuver = (Map<String, Object>) step.get("maneuver");
                        String instruction = step.getOrDefault("name", "").toString();
                        String modifier = maneuver != null ? (String) maneuver.getOrDefault("modifier", "") : "";
                        String type = maneuver != null ? (String) maneuver.getOrDefault("type", "") : "";

                        if (!instruction.isEmpty() || !type.isEmpty()) {
                            Map<String, String> s = new HashMap<>();
                            String desc = type + (!modifier.isEmpty() ? " " + modifier : "")
                                    + (!instruction.isEmpty() ? " on " + instruction : "");
                            s.put("instruction", desc.trim());
                            double stepDist = ((Number) step.getOrDefault("distance", 0)).doubleValue();
                            double stepDur = ((Number) step.getOrDefault("duration", 0)).doubleValue();
                            s.put("distance", String.format("%.1f km", stepDist / 1000.0));
                            s.put("duration", formatDuration((int) (stepDur / 60.0)));
                            simpleSteps.add(s);
                        }
                    }
                }
            }
        }
        result.put("steps", simpleSteps);
        return result;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseOverpassResponse(Map<String, Object> raw, String type) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> elements = (List<Map<String, Object>>) raw.getOrDefault("elements", List.of());

        List<Map<String, Object>> places = new ArrayList<>();
        for (Map<String, Object> element : elements) {
            Map<String, Object> tags = (Map<String, Object>) element.getOrDefault("tags", Map.of());
            Map<String, Object> p = new HashMap<>();
            p.put("name", tags.getOrDefault("name", "Unnamed"));
            String addr = buildAddress(tags);
            p.put("address", addr.isEmpty() ? "Address not available" : addr);
            p.put("types", List.of(type));
            p.put("source", "OpenStreetMap");
            p.put("latitude", element.get("lat"));
            p.put("longitude", element.get("lon"));

            // OSM doesn't have ratings — leave absent
            if (tags.containsKey("stars")) {
                try {
                    p.put("rating", Double.parseDouble(tags.get("stars").toString()));
                } catch (NumberFormatException ignored) {}
            }
            if (tags.containsKey("website")) {
                p.put("website", tags.get("website"));
            }
            if (tags.containsKey("phone")) {
                p.put("phone", tags.get("phone"));
            }
            places.add(p);
        }

        result.put("status", "success");
        result.put("source", "OpenStreetMap");
        result.put("type", type);
        result.put("count", places.size());
        result.put("places", places);
        return result;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseOsrmTableResponse(Map<String, Object> raw,
                                                         List<String> originNames,
                                                         List<String> destNames) {
        Map<String, Object> result = new HashMap<>();

        String code = (String) raw.getOrDefault("code", "Error");
        if (!"Ok".equals(code)) {
            result.put("status", "error");
            result.put("message", "OSRM Table returned: " + code);
            return result;
        }

        List<List<Number>> durations = (List<List<Number>>) raw.get("durations");
        List<List<Number>> distances = (List<List<Number>>) raw.get("distances");

        List<Map<String, Object>> matrix = new ArrayList<>();
        for (int i = 0; i < originNames.size(); i++) {
            for (int j = 0; j < destNames.size(); j++) {
                Map<String, Object> entry = new HashMap<>();
                entry.put("origin", originNames.get(i));
                entry.put("destination", destNames.get(j));

                if (durations != null && distances != null
                        && i < durations.size() && j < durations.get(i).size()) {
                    double durSec = durations.get(i).get(j).doubleValue();
                    double distM = distances.get(i).get(j).doubleValue();
                    int durMin = (int) (durSec / 60.0);
                    double distKm = distM / 1000.0;

                    entry.put("distance", String.format("%.0f km", distKm));
                    entry.put("distanceMeters", (int) distM);
                    entry.put("duration", formatDuration(durMin));
                    entry.put("durationSeconds", (int) durSec);
                } else {
                    entry.put("error", "No route available");
                }
                matrix.add(entry);
            }
        }

        result.put("status", "success");
        result.put("source", "OpenStreetMap (OSRM)");
        result.put("matrix", matrix);
        return result;
    }

    // ──────────────────────────────────────────────
    //  Helpers
    // ──────────────────────────────────────────────

    private String mapModeToOsrmProfile(String mode) {
        if (mode == null) return "driving";
        return switch (mode.toLowerCase()) {
            case "walking" -> "foot";
            case "bicycling", "cycling" -> "bike";
            default -> "driving";
        };
    }

    private String mapTypeToOverpassFilter(String type) {
        return switch (type != null ? type.toLowerCase() : "") {
            case "lodging", "hotel" ->
                    "node[\"tourism\"=\"hotel\"](around:{radius},{lat},{lon});"
                    + "node[\"tourism\"=\"guest_house\"](around:{radius},{lat},{lon});";
            case "restaurant" ->
                    "node[\"amenity\"=\"restaurant\"](around:{radius},{lat},{lon});";
            case "tourist_attraction" ->
                    "node[\"tourism\"=\"attraction\"](around:{radius},{lat},{lon});"
                    + "node[\"tourism\"=\"museum\"](around:{radius},{lat},{lon});"
                    + "node[\"historic\"](around:{radius},{lat},{lon});";
            case "car_rental" ->
                    "node[\"amenity\"=\"car_rental\"](around:{radius},{lat},{lon});";
            default ->
                    "node[\"amenity\"=\"" + type + "\"](around:{radius},{lat},{lon});";
        };
    }

    private String buildAddress(Map<String, Object> tags) {
        StringBuilder sb = new StringBuilder();
        String street = (String) tags.get("addr:street");
        String houseNumber = (String) tags.get("addr:housenumber");
        String city = (String) tags.get("addr:city");

        if (houseNumber != null) sb.append(houseNumber).append(" ");
        if (street != null) sb.append(street);
        if (city != null) {
            if (!sb.isEmpty()) sb.append(", ");
            sb.append(city);
        }
        return sb.toString();
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
        result.put("placeId", "osm_dummy_" + address.replaceAll("\\s+", "_"));
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
        p.put("types", types);
        p.put("source", "Dummy Data (dev mode)");
        p.put("latitude", lat);
        p.put("longitude", lng);
        return p;
    }

    private Map<String, Object> errorResult(String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "error");
        result.put("message", message);
        return result;
    }
}
