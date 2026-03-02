package com.travelplan.event.dto;

import com.travelplan.event.model.enums.EventCategory;
import com.travelplan.event.model.enums.EventStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * TASK-7.1: A single event returned by the route-based event finder.
 * Extends the normal event summary with a distance-from-route field
 * so the tourist can see how far off their route the event is.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteEventResult {
    private Long id;
    private String title;
    private EventCategory category;
    private String location;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private OffsetDateTime startDateTime;
    private OffsetDateTime endDateTime;
    private Integer availableSeats;
    private BigDecimal ticketPrice;
    private String currency;
    private EventStatus status;
    private String imageUrl;
    private List<String> vibes;
    private boolean authenticCultural;

    /**
     * Perpendicular distance (in km) from this event's location to the
     * nearest point on the requested travel route segment.
     * A value of 0.0 means the event is directly on the route.
     */
    private double distanceFromRouteKm;
}
