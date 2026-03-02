package com.travelplan.event.service;

import com.travelplan.event.dto.BookTicketRequest;
import com.travelplan.event.dto.CancelTicketRequest;
import com.travelplan.event.dto.TicketResponse;
import com.travelplan.common.dto.PaginatedResponse;
import com.travelplan.event.model.enums.RegistrationStatus;
import org.springframework.data.domain.Pageable;

public interface EventRegistrationService {
    TicketResponse bookTickets(Long eventId, BookTicketRequest request, String touristId);

    PaginatedResponse<TicketResponse> getEventRegistrations(Long eventId, RegistrationStatus status, String organizerId,
            Pageable pageable);

    TicketResponse getTicket(Long eventId, Long ticketId, String userId);

    TicketResponse cancelBooking(Long eventId, Long ticketId, CancelTicketRequest request, String touristId);

    PaginatedResponse<TicketResponse> getTouristTickets(String touristId, RegistrationStatus status, Pageable pageable);
}
