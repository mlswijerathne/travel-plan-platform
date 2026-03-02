package com.travelplan.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * TASK-7.2: Regional Pulse — aggregated event metrics for a geographic district.
 *
 * Gives the AI Agent (and tourists) a single-shot overview of what is happening
 * within a radius of a given location: event counts, category breakdown,
 * pricing, seat availability, and the next upcoming events.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegionalPulseResponse {

    /** Human-readable district / area label (passed by caller or resolved from coordinates). */
    private String district;

    /** Centre point of the search. */
    private Double centerLat;
    private Double centerLng;

    /** Search radius used for this aggregation (km). */
    private Double radiusKm;

    /** Total PUBLISHED events with coordinates within the radius. */
    private int totalEvents;

    /** Total available seats summed across all matching events. */
    private int totalAvailableSeats;

    /** Average ticket price across matching events (null if no events). */
    private BigDecimal averageTicketPrice;

    /** Min ticket price among matching events (null if no events). */
    private BigDecimal minTicketPrice;

    /** Max ticket price among matching events (null if no events). */
    private BigDecimal maxTicketPrice;

    /** Number of matching events where availableSeats > 0. */
    private int eventsWithSeatsAvailable;

    /** Event count per category, e.g. {"CONCERT": 3, "FESTIVAL": 1}. */
    private Map<String, Long> categoryBreakdown;

    /** Number of authentic cultural heritage events in the region. */
    private long authenticCulturalCount;

    /** Up to 5 soonest upcoming PUBLISHED events in the region (lightweight summaries). */
    private List<EventSummaryResponse> upcomingEvents;
}
