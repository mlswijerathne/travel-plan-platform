package com.travelplan.event.controller;

import com.travelplan.common.dto.ApiResponse;
import com.travelplan.event.dto.RegionalPulseResponse;
import com.travelplan.event.dto.RouteEventResult;
import com.travelplan.event.dto.RouteSearchRequest;
import com.travelplan.event.service.GeospatialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * EPIC-7: Proactive Geospatial Discovery
 *
 * Provides two geospatial endpoints:
 *   - TASK-7.1  GET /api/geo/events/route          — Route-based event corridor search
 *   - TASK-7.2  GET /api/geo/events/regional-pulse — District-level aggregated pulse
 *
 * Accessible to any authenticated principal (TOURIST, ORGANIZER, or AI Agent).
 */
@RestController
@RequestMapping("/api/geo/events")
@RequiredArgsConstructor
@Tag(name = "Geospatial Discovery (EPIC-7)",
     description = "Route-based and regional event discovery powered by Haversine geometry")
public class GeospatialController {

    private final GeospatialService geospatialService;

    // -------------------------------------------------------------------------
    // TASK-7.1 — Route-Based Event Finder
    // -------------------------------------------------------------------------

    /**
     * Finds PUBLISHED events whose location falls within {@code radiusKm} km of the
     * straight-line travel segment from (startLat, startLng) to (endLat, endLng).
     *
     * Algorithm overview:
     *   1. SQL bounding-box pre-filter (cheap, index-assisted).
     *   2. Java-side Haversine point-to-segment distance filter (accurate).
     *   3. Results include {@code distanceFromRouteKm} and are ordered ascending.
     *
     * Example (Colombo → Kandy, 50 km corridor):
     *   GET /api/geo/events/route?startLat=6.9271&startLng=79.8612&endLat=7.2906&endLng=80.6337&radiusKm=50
     */
    @GetMapping("/route")
    @Operation(
            summary = "Find events along a travel route (TASK-7.1)",
            description = "Returns PUBLISHED events within radiusKm of the route segment A→B, " +
                    "sorted by distance from route. Each result includes distanceFromRouteKm."
    )
    public ResponseEntity<ApiResponse<List<RouteEventResult>>> findEventsAlongRoute(
            @Parameter(description = "Start latitude",  example = "6.9271")  @RequestParam Double startLat,
            @Parameter(description = "Start longitude", example = "79.8612") @RequestParam Double startLng,
            @Parameter(description = "End latitude",    example = "7.2906")  @RequestParam Double endLat,
            @Parameter(description = "End longitude",   example = "80.6337") @RequestParam Double endLng,
            @Parameter(description = "Corridor half-width in km (default 50)") @RequestParam(defaultValue = "50") Double radiusKm,
            @Parameter(description = "Only events on/after this date")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @Parameter(description = "Only events on/before this date")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {

        RouteSearchRequest req = RouteSearchRequest.builder()
                .startLat(startLat).startLng(startLng)
                .endLat(endLat).endLng(endLng)
                .radiusKm(radiusKm)
                .dateFrom(dateFrom)
                .dateTo(dateTo)
                .build();

        List<RouteEventResult> results = geospatialService.findEventsAlongRoute(req);
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    // -------------------------------------------------------------------------
    // TASK-7.2 — Regional Pulse Aggregation
    // -------------------------------------------------------------------------

    /**
     * Returns aggregated event statistics within {@code radiusKm} km of the given
     * centre point, including category breakdown, seat availability, pricing stats,
     * cultural heritage count, and the 5 soonest upcoming events.
     *
     * Example (Kandy district, 30 km radius):
     *   GET /api/geo/events/regional-pulse?district=Kandy&lat=7.2906&lng=80.6337&radiusKm=30
     */
    @GetMapping("/regional-pulse")
    @Operation(
            summary = "Regional event pulse for a district (TASK-7.2)",
            description = "Aggregates PUBLISHED events within radiusKm of the centre point. " +
                    "Returns total counts, category breakdown, pricing stats, cultural count, " +
                    "and a preview of the 5 next upcoming events."
    )
    public ResponseEntity<ApiResponse<RegionalPulseResponse>> getRegionalPulse(
            @Parameter(description = "Human-readable district label", example = "Kandy")
            @RequestParam(defaultValue = "Unknown") String district,
            @Parameter(description = "Centre latitude",  example = "7.2906")  @RequestParam Double lat,
            @Parameter(description = "Centre longitude", example = "80.6337") @RequestParam Double lng,
            @Parameter(description = "Search radius in km (default 30)") @RequestParam(defaultValue = "30") Double radiusKm) {

        RegionalPulseResponse pulse = geospatialService.getRegionalPulse(district, lat, lng, radiusKm);
        return ResponseEntity.ok(ApiResponse.success(pulse));
    }
}
