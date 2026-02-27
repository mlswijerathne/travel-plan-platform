package com.travelplan.event.controller;

import com.travelplan.common.dto.ApiResponse;
import com.travelplan.common.dto.PaginatedResponse;
import com.travelplan.event.dto.EventResponse;
import com.travelplan.event.dto.EventSummaryResponse;
import com.travelplan.event.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * EPIC-2 — Event Discovery (Tourist / Public)
 *
 * Public endpoints accessible without authentication.
 * All endpoints only expose PUBLISHED events.
 */
@RestController
@RequestMapping("/api/public/events")
@RequiredArgsConstructor
@Tag(name = "Event Discovery (Public)", description = "Public endpoints for tourists to browse and search events")
public class PublicEventController {

    private final EventService eventService;

    /**
     * STORY-2.1: Browse all published events (paginated, lightweight summary).
     * STORY-2.2: Search and filter events by category, location, date range, and price.
     * STORY-6.1: Filter by authentic cultural heritage badge.
     * STORY-6.2: Filter by mood-based vibe keyword (searches JSONB vibes array).
     * Results are ordered by startDateTime ascending.
     */
    @GetMapping
    @Operation(
            summary = "Browse and search published events",
            description = "Returns paginated list of PUBLISHED events. Supports optional filters: " +
                    "category, location (keyword), dateFrom, dateTo, minPrice, maxPrice, " +
                    "vibe (JSONB keyword tag), authenticCultural (cultural heritage badge). " +
                    "Results ordered by startDateTime ascending."
    )
    public ResponseEntity<PaginatedResponse<EventSummaryResponse>> browseEvents(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String vibe,
            @RequestParam(required = false) Boolean authenticCultural,
            @PageableDefault(size = 20, sort = "startDateTime", direction = Sort.Direction.ASC) Pageable pageable) {

        PaginatedResponse<EventSummaryResponse> response = eventService.browsePublishedEvents(
                category, location, dateFrom, dateTo, minPrice, maxPrice, vibe, authenticCultural, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * STORY-2.3: View full details of a specific published event.
     * Returns HTTP 404 if event is not found or is not PUBLISHED.
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Get full event details",
            description = "Returns full EventResponse for a PUBLISHED event. " +
                    "Returns HTTP 404 if the event does not exist or is not published."
    )
    public ResponseEntity<ApiResponse<EventResponse>> getEventDetails(@PathVariable Long id) {
        EventResponse response = eventService.getPublishedEvent(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
