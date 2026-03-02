package com.travelplan.event.service.impl;

import com.travelplan.common.dto.PaginatedResponse;
import com.travelplan.common.exception.ForbiddenException;
import com.travelplan.common.exception.ResourceNotFoundException;
import com.travelplan.common.exception.ValidationException;
import com.travelplan.event.dto.BookTicketRequest;
import com.travelplan.event.dto.CancelTicketRequest;
import com.travelplan.event.dto.TicketResponse;
import com.travelplan.event.mapper.EventMapper;
import com.travelplan.event.model.entity.Event;
import com.travelplan.event.model.entity.EventRegistration;
import com.travelplan.event.model.enums.EventStatus;
import com.travelplan.event.model.enums.RegistrationStatus;
import com.travelplan.event.repository.EventRegistrationRepository;
import com.travelplan.event.repository.EventRepository;
import com.travelplan.event.repository.TicketTierRepository;
import com.travelplan.event.service.DynamicPricingService;
import com.travelplan.event.service.EventRegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventRegistrationServiceImpl implements EventRegistrationService {

    private final EventRepository eventRepository;
    private final EventRegistrationRepository registrationRepository;
    private final TicketTierRepository ticketTierRepository;
    private final DynamicPricingService dynamicPricingService;
    private final EventMapper eventMapper;

    @Override
    @Transactional
    public TicketResponse bookTickets(Long eventId, BookTicketRequest request, String touristId) {
        log.info("Booking {} tickets for event {} by tourist {}", request.getNumberOfTickets(), eventId, touristId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));

        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new ValidationException("Cannot book tickets for an event that is not PUBLISHED");
        }

        com.travelplan.event.model.entity.TicketTier ticketTier = null;
        if (request.getTicketTierId() != null) {
            ticketTier = ticketTierRepository.findById(request.getTicketTierId())
                    .orElseThrow(() -> new ResourceNotFoundException("TicketTier", "id", request.getTicketTierId()));

            if (!ticketTier.getEvent().getId().equals(event.getId())) {
                throw new ValidationException("Ticket tier does not belong to this event");
            }

            if (ticketTier.getAvailableSeats() < request.getNumberOfTickets()) {
                throw new ValidationException(
                        "Not enough seats available in tier. Requested: " + request.getNumberOfTickets()
                                + ", Available: " + ticketTier.getAvailableSeats());
            }
        } else {
            if (event.getAvailableSeats() < request.getNumberOfTickets()) {
                throw new ValidationException("Not enough seats available. Requested: " + request.getNumberOfTickets()
                        + ", Available: " + event.getAvailableSeats());
            }
        }

        // Deduct seats
        event.setAvailableSeats(event.getAvailableSeats() - request.getNumberOfTickets());
        eventRepository.save(event);

        if (ticketTier != null) {
            ticketTier.setAvailableSeats(ticketTier.getAvailableSeats() - request.getNumberOfTickets());
            ticketTierRepository.save(ticketTier);
        }

        // Calculate dynamic price
        BigDecimal unitPrice = dynamicPricingService.calculatePrice(event, ticketTier);

        // Create registration
        EventRegistration registration = EventRegistration.builder()
                .event(event)
                .ticketTier(ticketTier)
                .touristId(touristId)
                .numTickets(request.getNumberOfTickets())
                .unitPrice(unitPrice)
                .totalAmount(unitPrice.multiply(BigDecimal.valueOf(request.getNumberOfTickets())))
                .ticketNumber(generateTicketNumber(event.getId()))
                .status(RegistrationStatus.CONFIRMED)
                .notes(request.getNotes())
                .build();

        EventRegistration savedRegistration = registrationRepository.save(registration);
        return eventMapper.toTicketResponse(savedRegistration);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<TicketResponse> getEventRegistrations(Long eventId, RegistrationStatus status,
            String organizerId, Pageable pageable) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));

        if (!event.getOrganizerId().equals(organizerId)) {
            throw new ForbiddenException("Only the organizer can view registrations for this event");
        }

        Page<EventRegistration> regPage;
        if (status != null) {
            regPage = registrationRepository.findByEventIdAndStatus(eventId, status, pageable);
        } else {
            regPage = registrationRepository.findByEventId(eventId, pageable);
        }

        List<TicketResponse> content = regPage.getContent().stream()
                .map(eventMapper::toTicketResponse)
                .toList();

        return PaginatedResponse.of(content, regPage.getNumber(), regPage.getSize(), regPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public TicketResponse getTicket(Long eventId, Long ticketId, String userId) {
        EventRegistration registration = registrationRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Registration", "id", ticketId));

        // Validate the registration belongs to the requested event
        if (!registration.getEvent().getId().equals(eventId)) {
            throw new ResourceNotFoundException("Registration", "id", ticketId);
        }

        // Security check: Either user is the tourist or the organizer
        boolean isTourist = registration.getTouristId().equals(userId);
        boolean isOrganizer = registration.getEvent().getOrganizerId().equals(userId);

        if (!isTourist && !isOrganizer) {
            throw new ForbiddenException("You are not authorized to view this ticket");
        }

        return eventMapper.toTicketResponse(registration);
    }

    @Override
    @Transactional
    public TicketResponse cancelBooking(Long eventId, Long ticketId, CancelTicketRequest request, String touristId) {
        EventRegistration registration = registrationRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Registration", "id", ticketId));

        if (!registration.getTouristId().equals(touristId)) {
            throw new ForbiddenException("You can only cancel your own bookings");
        }

        if (registration.getStatus() == RegistrationStatus.CANCELLED) {
            throw new ValidationException("Ticket is already cancelled");
        }

        // Restore seats
        Event event = registration.getEvent();
        event.setAvailableSeats(event.getAvailableSeats() + registration.getNumTickets());
        eventRepository.save(event);

        if (registration.getTicketTier() != null) {
            com.travelplan.event.model.entity.TicketTier ticketTier = registration.getTicketTier();
            ticketTier.setAvailableSeats(ticketTier.getAvailableSeats() + registration.getNumTickets());
            ticketTierRepository.save(ticketTier);
        }

        // Update registration
        registration.setStatus(RegistrationStatus.CANCELLED);
        registration.setCancellationReason(request.getReason());
        registration.setCancelledAt(OffsetDateTime.now());

        EventRegistration updatedRegistration = registrationRepository.save(registration);
        return eventMapper.toTicketResponse(updatedRegistration);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<TicketResponse> getTouristTickets(String touristId, RegistrationStatus status,
            Pageable pageable) {
        Page<EventRegistration> regPage;
        if (status != null) {
            regPage = registrationRepository.findByTouristIdAndStatus(touristId, status, pageable);
        } else {
            regPage = registrationRepository.findByTouristId(touristId, pageable);
        }

        List<TicketResponse> content = regPage.getContent().stream()
                .map(eventMapper::toTicketResponse)
                .toList();

        return PaginatedResponse.of(content, regPage.getNumber(), regPage.getSize(), regPage.getTotalElements());
    }

    private String generateTicketNumber(Long eventId) {
        // Format: EVT-{eventId}-TKT-{yyyyMMdd}-{random4}
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomPart = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        return String.format("EVT-%d-TKT-%s-%s", eventId, datePart, randomPart);
    }
}
