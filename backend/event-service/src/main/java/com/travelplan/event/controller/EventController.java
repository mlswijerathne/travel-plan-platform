package com.travelplan.event.controller;

import com.travelplan.common.dto.ApiResponse;
import com.travelplan.common.dto.PaginatedResponse;
import com.travelplan.event.dto.*;
import com.travelplan.event.model.enums.EventStatus;
import com.travelplan.event.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Tag(name = "Event Management", description = "Endpoints for creating and browsing events")
public class EventController {

    private final EventService eventService;

    @PostMapping
    @PreAuthorize("hasRole('ORGANIZER')")
    @Operation(summary = "Create a new event")
    public ResponseEntity<ApiResponse<EventResponse>> createEvent(
            @Valid @RequestBody CreateEventRequest request,
            Authentication authentication) {
        String organizerId = authentication.getName();
        EventResponse response = eventService.createEvent(request, organizerId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get full event details by ID")
    public ResponseEntity<ApiResponse<EventResponse>> getEvent(@PathVariable Long id) {
        EventResponse response = eventService.getEvent(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "Search and browse events with filters (EPIC-6: vibe & cultural heritage)")
    public ResponseEntity<PaginatedResponse<EventSummaryResponse>> searchEvents(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "PUBLISHED") EventStatus status,
            @RequestParam(required = false) String vibe,
            @RequestParam(required = false) Boolean authenticCultural,
            @PageableDefault(size = 20) Pageable pageable) {

        PaginatedResponse<EventSummaryResponse> response = eventService.searchEvents(
                category, location, dateFrom, dateTo, minPrice, maxPrice, status,
                vibe, authenticCultural, pageable);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ORGANIZER')")
    @Operation(summary = "Update event details")
    public ResponseEntity<ApiResponse<EventResponse>> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody UpdateEventRequest request,
            Authentication authentication) {
        String organizerId = authentication.getName();
        EventResponse response = eventService.updateEvent(id, request, organizerId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ORGANIZER')")
    @Operation(summary = "Cancel or delete an event")
    public ResponseEntity<ApiResponse<Void>> deleteEvent(
            @PathVariable Long id,
            Authentication authentication) {
        String organizerId = authentication.getName();
        eventService.deleteEvent(id, organizerId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/{id}/availability")
    @Operation(summary = "Check seat availability (Internal use)")
    public ResponseEntity<ApiResponse<EventAvailabilityResponse>> checkAvailability(@PathVariable Long id) {
        EventAvailabilityResponse response = eventService.checkAvailability(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/organizer/my")
    @PreAuthorize("hasRole('ORGANIZER')")
    @Operation(summary = "Get all events created by the logged-in organizer")
    public ResponseEntity<PaginatedResponse<EventSummaryResponse>> getMyEvents(
            @RequestParam(required = false) EventStatus status,
            @PageableDefault(size = 20, sort = "startDateTime", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable,
            Authentication authentication) {
        String organizerId = authentication.getName();
        PaginatedResponse<EventSummaryResponse> response = eventService.getOrganizerEvents(organizerId, status,
                pageable);
        return ResponseEntity.ok(response);
    }
}
