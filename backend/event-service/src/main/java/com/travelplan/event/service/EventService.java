package com.travelplan.event.service;

import com.travelplan.event.dto.*;
import com.travelplan.event.model.enums.EventStatus;
import com.travelplan.common.dto.PaginatedResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface EventService {
    EventResponse createEvent(CreateEventRequest request, String organizerId);

    EventResponse getEvent(Long id);

    /**
     * STORY-2.1 / 2.2: Browse and search published events (public, tourist-facing).
     * Always filters to PUBLISHED status. Results ordered by startDateTime ASC.
     * STORY-6.1: authenticCultural flag filter.
     * STORY-6.2: vibe keyword search across JSONB vibes array.
     */
    PaginatedResponse<EventSummaryResponse> browsePublishedEvents(
            String category, String location, LocalDate dateFrom, LocalDate dateTo,
            Double minPrice, Double maxPrice, String vibe, Boolean authenticCultural, Pageable pageable);

    /**
     * STORY-2.3: Get full event details for a specific published event (public, tourist-facing).
     * Throws ResourceNotFoundException if event is not found or is not PUBLISHED.
     */
    EventResponse getPublishedEvent(Long id);

    /**
     * STORY-6.1 / 6.2: Admin/organizer search with optional vibe and cultural heritage filters.
     */
    PaginatedResponse<EventSummaryResponse> searchEvents(
            String category, String location, LocalDate dateFrom, LocalDate dateTo,
            Double minPrice, Double maxPrice, EventStatus status, String vibe,
            Boolean authenticCultural, Pageable pageable);

    EventResponse updateEvent(Long id, UpdateEventRequest request, String organizerId);

    void deleteEvent(Long id, String organizerId);

    EventAvailabilityResponse checkAvailability(Long id);

    PaginatedResponse<EventSummaryResponse> getOrganizerEvents(String organizerId, EventStatus status,
            Pageable pageable);
}
