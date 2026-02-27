package com.travelplan.event.mapper;

import com.travelplan.event.dto.*;
import com.travelplan.event.model.entity.Event;
import com.travelplan.event.model.entity.EventRegistration;
import com.travelplan.event.model.entity.TicketTier;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EventMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "availableSeats", source = "totalCapacity")
    @Mapping(target = "status", constant = "DRAFT")
    @Mapping(target = "organizerId", ignore = true)
    @Mapping(target = "ticketTiers", source = "ticketTiers")
    Event toEntity(CreateEventRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "organizerId", ignore = true)
    @Mapping(target = "registrations", ignore = true)
    @Mapping(target = "ticketTiers", ignore = true) // Handle separately if needed
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntity(UpdateEventRequest request, @MappingTarget Event event);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "event", ignore = true)
    @Mapping(target = "availableSeats", source = "capacity")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    TicketTier toEntity(TicketTierRequest request);

    TicketTierResponse toResponse(TicketTier tier);

    @Mapping(target = "organizerName", ignore = true)
    EventResponse toResponse(Event event);

    EventSummaryResponse toSummaryResponse(Event event);

    @Mapping(target = "eventId", source = "id")
    @Mapping(target = "eventTitle", source = "title")
    @Mapping(target = "available", expression = "java(event.getAvailableSeats() > 0 && event.getStatus() == com.travelplan.event.model.enums.EventStatus.PUBLISHED)")
    @Mapping(target = "ticketTiers", source = "ticketTiers")
    EventAvailabilityResponse toAvailabilityResponse(Event event);

    @Mapping(target = "eventId", source = "event.id")
    @Mapping(target = "eventTitle", source = "event.title")
    @Mapping(target = "eventStartDateTime", source = "event.startDateTime")
    @Mapping(target = "eventLocation", source = "event.location")
    @Mapping(target = "ticketTierId", source = "ticketTier.id")
    @Mapping(target = "ticketTierName", source = "ticketTier.name")
    @Mapping(target = "numberOfTickets", source = "numTickets")
    TicketResponse toTicketResponse(EventRegistration registration);

}
