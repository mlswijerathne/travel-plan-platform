package com.travelplan.event.service.impl;

import com.travelplan.common.exception.ForbiddenException;
import com.travelplan.common.exception.ResourceNotFoundException;
import com.travelplan.common.exception.ValidationException;
import com.travelplan.event.dto.CreateEventRequest;
import com.travelplan.event.dto.EventResponse;
import com.travelplan.event.dto.UpdateEventRequest;
import com.travelplan.event.mapper.EventMapper;
import com.travelplan.event.model.entity.Event;
import com.travelplan.event.model.enums.EventStatus;
import com.travelplan.event.repository.EventRegistrationRepository;
import com.travelplan.event.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventRegistrationRepository eventRegistrationRepository;

    @Mock
    private EventMapper eventMapper;

    @InjectMocks
    private EventServiceImpl eventService;

    private Event draftEvent;
    private Event publishedEvent;
    private String organizerId = "organizer-123";

    @BeforeEach
    void setUp() {
        draftEvent = Event.builder()
                .id(1L)
                .organizerId(organizerId)
                .title("Draft Event")
                .totalCapacity(100)
                .availableSeats(100)
                .status(EventStatus.DRAFT)
                .build();

        publishedEvent = Event.builder()
                .id(2L)
                .organizerId(organizerId)
                .title("Published Event")
                .totalCapacity(50)
                .availableSeats(50)
                .status(EventStatus.PUBLISHED)
                .build();
    }

    @Test
    void createEvent_Success() {
        CreateEventRequest request = new CreateEventRequest();
        request.setTitle("New Event");

        Event mappedEvent = new Event();
        mappedEvent.setTitle("New Event");

        Event savedEvent = new Event();
        savedEvent.setId(1L);
        savedEvent.setTitle("New Event");
        savedEvent.setOrganizerId(organizerId);

        EventResponse response = new EventResponse();
        response.setId(1L);

        when(eventMapper.toEntity(request)).thenReturn(mappedEvent);
        when(eventRepository.save(mappedEvent)).thenReturn(savedEvent);
        when(eventMapper.toResponse(savedEvent)).thenReturn(response);

        EventResponse result = eventService.createEvent(request, organizerId);

        assertThat(result.getId()).isEqualTo(1L);
        verify(eventRepository).save(mappedEvent);
        assertThat(mappedEvent.getOrganizerId()).isEqualTo(organizerId); // Ensure organizer ID is set
    }

    @Test
    void updateEvent_Success() {
        UpdateEventRequest request = new UpdateEventRequest();
        request.setTotalCapacity(120);

        EventResponse response = new EventResponse();

        when(eventRepository.findById(1L)).thenReturn(Optional.of(draftEvent));
        when(eventRepository.save(draftEvent)).thenReturn(draftEvent);
        when(eventMapper.toResponse(draftEvent)).thenReturn(response);

        EventResponse result = eventService.updateEvent(1L, request, organizerId);

        verify(eventMapper).updateEntity(request, draftEvent);
        verify(eventRepository).save(draftEvent);
        assertThat(draftEvent.getAvailableSeats()).isEqualTo(120);
    }

    @Test
    void updateEvent_Forbidden() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(draftEvent));

        assertThrows(ForbiddenException.class, () -> {
            eventService.updateEvent(1L, new UpdateEventRequest(), "wrong-organizer");
        });

        verify(eventRepository, never()).save(any());
    }

    @Test
    void updateEvent_CapacityConstraintFail() {
        draftEvent.setAvailableSeats(80); // 20 booked seats
        UpdateEventRequest request = new UpdateEventRequest();
        request.setTotalCapacity(10); // Trying to set capacity below 20

        when(eventRepository.findById(1L)).thenReturn(Optional.of(draftEvent));

        ValidationException ex = assertThrows(ValidationException.class, () -> {
            eventService.updateEvent(1L, request, organizerId);
        });

        assertThat(ex.getMessage()).contains("cannot be less than already booked seats");
    }

    @Test
    void publishEvent_Success() {
        UpdateEventRequest request = new UpdateEventRequest();
        request.setStatus(EventStatus.PUBLISHED);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(draftEvent));
        when(eventRepository.save(draftEvent)).thenReturn(draftEvent);

        eventService.updateEvent(1L, request, organizerId);

        verify(eventMapper).updateEntity(request, draftEvent);
        verify(eventRepository).save(draftEvent);
    }

    @Test
    void publishEvent_Fail_IfAlreadyPublished() {
        UpdateEventRequest request = new UpdateEventRequest();
        request.setStatus(EventStatus.PUBLISHED);

        when(eventRepository.findById(2L)).thenReturn(Optional.of(publishedEvent));

        assertThrows(ValidationException.class, () -> {
            eventService.updateEvent(2L, request, organizerId);
        });
    }

    @Test
    void deleteEvent_Success() {
        when(eventRepository.findById(2L)).thenReturn(Optional.of(publishedEvent));

        eventService.deleteEvent(2L, organizerId);

        assertThat(publishedEvent.getStatus()).isEqualTo(EventStatus.CANCELLED);
        verify(eventRepository).save(publishedEvent);
        verify(eventRegistrationRepository).findByEventId(2L);
    }
}
