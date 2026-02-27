package com.travelplan.event.service;

import com.travelplan.event.dto.RegionalPulseResponse;
import com.travelplan.event.dto.RouteEventResult;
import com.travelplan.event.dto.RouteSearchRequest;

import java.util.List;

/**
 * EPIC-7: Proactive Geospatial Discovery.
 */
public interface GeospatialService {

    /**
     * TASK-7.1 — Route-Based Event Finder.
     *
     * Returns PUBLISHED events that lie within a corridor of {@code radiusKm}
     * either side of the straight-line travel segment from
     * (startLat, startLng) to (endLat, endLng).
     *
     * Each result includes the perpendicular distance from the event to the route
     * so the tourist can judge how far off-route a detour would be.
     * Results are ordered by startDateTime ascending (soonest events first).
     */
    List<RouteEventResult> findEventsAlongRoute(RouteSearchRequest request);

    /**
     * TASK-7.2 — Regional Pulse Aggregation.
     *
     * Returns aggregated statistics for all PUBLISHED events within
     * {@code radiusKm} km of (lat, lng), including total count, category
     * breakdown, pricing stats, and the next 5 upcoming events.
     *
     * @param district  display label for the district (e.g. "Kandy", "Colombo")
     * @param lat       centre latitude
     * @param lng       centre longitude
     * @param radiusKm  search radius in km (default 30)
     */
    RegionalPulseResponse getRegionalPulse(String district, double lat, double lng, double radiusKm);
}
