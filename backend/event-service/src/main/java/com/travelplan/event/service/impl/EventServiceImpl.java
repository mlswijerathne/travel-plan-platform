package com.travelplan.event.service.impl;

import com.travelplan.common.dto.PaginatedResponse;
import com.travelplan.common.exception.ForbiddenException;
import com.travelplan.common.exception.ResourceNotFoundException;
import com.travelplan.common.exception.ValidationException;
import com.travelplan.event.dto.*;
import com.travelplan.event.mapper.EventMapper;
import com.travelplan.event.model.entity.Event;
import com.travelplan.event.model.enums.EventStatus;
import com.travelplan.event.model.enums.RegistrationStatus;
import com.travelplan.event.repository.EventRegistrationRepository;
import com.travelplan.event.repository.EventRepository;
import com.travelplan.event.service.EventService;
import com.travelplan.event.repository.EventSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final EventRegistrationRepository eventRegistrationRepository;
    private final EventMapper eventMapper;

    @Override
    @Transactional
    public EventResponse createEvent(CreateEventRequest request, String organizerId) {
        log.info("Creating event: {} for organizer: {}", request.getTitle(), organizerId);
        Event event = eventMapper.toEntity(request);
        event.setOrganizerId(organizerId);

        // Set back-reference on each tier so cascade save can populate event_id (NOT NULL)
        if (event.getTicketTiers() != null) {
            event.getTicketTiers().forEach(tier -> tier.setEvent(event));
        }

        Event savedEvent = eventRepository.save(event);
        return eventMapper.toResponse(savedEvent);
    }

    @Override
    @Transactional(readOnly = true)
    public EventResponse getEvent(Long id) {
        Event event = findEventById(id);
        return eventMapper.toResponse(event);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<EventSummaryResponse> browsePublishedEvents(
            String category, String location, LocalDate dateFrom, LocalDate dateTo,
            Double minPrice, Double maxPrice, String vibe, Boolean authenticCultural, Pageable pageable) {

        // STORY-6.1 / 6.2: EventSpecification composes all filters including vibe JSONB search and cultural flag
        Specification<Event> spec = EventSpecification.browsePublished(
                category, location, dateFrom, dateTo, minPrice, maxPrice, vibe, authenticCultural);

        // STORY-2.2: order by startDateTime ascending (applied as a wrapping spec)
        Specification<Event> orderedSpec = (root, query, cb) -> {
            if (query != null && !Long.class.equals(query.getResultType())) {
                query.orderBy(cb.asc(root.get("startDateTime")));
            }
            return spec.toPredicate(root, query, cb);
        };

        Page<Event> eventPage = eventRepository.findAll(orderedSpec, pageable);
        List<EventSummaryResponse> content = eventPage.getContent().stream()
                .map(eventMapper::toSummaryResponse)
                .toList();

        return PaginatedResponse.of(content, eventPage.getNumber(), eventPage.getSize(), eventPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public EventResponse getPublishedEvent(Long id) {
        Event event = findEventById(id);
        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new com.travelplan.common.exception.ResourceNotFoundException("Event", "id", id);
        }
        return eventMapper.toResponse(event);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<EventSummaryResponse> searchEvents(
            String category, String location, LocalDate dateFrom, LocalDate dateTo,
            Double minPrice, Double maxPrice, EventStatus status, String vibe,
            Boolean authenticCultural, Pageable pageable) {

        // STORY-6.1 / 6.2: Full composable spec including JSONB vibe search and cultural flag
        Specification<Event> spec = EventSpecification.search(
                category, location, dateFrom, dateTo, minPrice, maxPrice, status, vibe, authenticCultural);

        Page<Event> eventPage = eventRepository.findAll(spec, pageable);
        List<EventSummaryResponse> content = eventPage.getContent().stream()
                .map(eventMapper::toSummaryResponse)
                .toList();

        return PaginatedResponse.of(content, eventPage.getNumber(), eventPage.getSize(), eventPage.getTotalElements());
    }

    @Override
    @Transactional
    public EventResponse updateEvent(Long id, UpdateEventRequest request, String organizerId) {
        Event event = findEventById(id);

        if (!event.getOrganizerId().equals(organizerId)) {
            throw new ForbiddenException("You are not authorized to update this event");
        }

        if (event.getStatus() == EventStatus.CANCELLED || event.getStatus() == EventStatus.COMPLETED) {
            throw new ValidationException("Cannot update a cancelled or completed event");
        }

        if (request.getStatus() != null && request.getStatus() == EventStatus.PUBLISHED) {
            if (event.getStatus() != EventStatus.DRAFT) {
                throw new ValidationException("Only DRAFT events can be published");
            }
        }

        if (request.getTotalCapacity() != null) {
            int bookedSeats = event.getTotalCapacity() - event.getAvailableSeats();
            if (request.getTotalCapacity() < bookedSeats) {
                throw new ValidationException(
                        "Total capacity cannot be less than already booked seats (" + bookedSeats + ")");
            }
            event.setAvailableSeats(request.getTotalCapacity() - bookedSeats);
        }

        eventMapper.updateEntity(request, event);
        Event updatedEvent = eventRepository.save(event);
        return eventMapper.toResponse(updatedEvent);
    }

    @Override
    @Transactional
    public void deleteEvent(Long id, String organizerId) {
        Event event = findEventById(id);

        if (!event.getOrganizerId().equals(organizerId)) {
            throw new ForbiddenException("You are not authorized to cancel this event");
        }

        if (event.getStatus() == EventStatus.COMPLETED) {
            throw new ValidationException("Cannot cancel a completed event");
        }

        event.setStatus(EventStatus.CANCELLED);

        // Cancel all registrations
        java.util.List<com.travelplan.event.model.entity.EventRegistration> registrations = eventRegistrationRepository
                .findByEventId(id);
        registrations.stream()
                .filter(reg -> reg.getStatus() == RegistrationStatus.CONFIRMED)
                .forEach(reg -> {
                    reg.setStatus(RegistrationStatus.CANCELLED);
                    reg.setCancellationReason("Event was cancelled by organizer");
                    reg.setCancelledAt(java.time.OffsetDateTime.now());
                });
        eventRegistrationRepository.saveAll(registrations);

        eventRepository.save(event);
    }

    @Override
    @Transactional(readOnly = true)
    public EventAvailabilityResponse checkAvailability(Long id) {
        Event event = findEventById(id);
        return eventMapper.toAvailabilityResponse(event);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<EventSummaryResponse> getOrganizerEvents(String organizerId, EventStatus status,
            Pageable pageable) {
        Page<Event> eventPage;
        if (status != null) {
            eventPage = eventRepository.findByOrganizerIdAndStatus(organizerId, status, pageable);
        } else {
            eventPage = eventRepository.findByOrganizerId(organizerId, pageable);
        }

        List<EventSummaryResponse> content = eventPage.getContent().stream()
                .map(eventMapper::toSummaryResponse)
                .toList();

        return PaginatedResponse.of(content, eventPage.getNumber(), eventPage.getSize(), eventPage.getTotalElements());
    }

    private Event findEventById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", id));
    }
}
