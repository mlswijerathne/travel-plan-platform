package com.travelplan.event.controller;

import com.travelplan.common.dto.ApiResponse;
import com.travelplan.common.dto.PaginatedResponse;
import com.travelplan.event.dto.BookTicketRequest;
import com.travelplan.event.dto.CancelTicketRequest;
import com.travelplan.event.dto.TicketResponse;
import com.travelplan.event.model.enums.RegistrationStatus;
import com.travelplan.event.service.EventRegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Tag(name = "Ticket Registrations", description = "Endpoints for booking and managing event tickets")
public class EventRegistrationController {

    private final EventRegistrationService registrationService;

    @PostMapping("/{id}/registrations")
    @PreAuthorize("hasRole('TOURIST')")
    @Operation(summary = "Book tickets for an event")
    public ResponseEntity<ApiResponse<TicketResponse>> bookTickets(
            @PathVariable Long id,
            @Valid @RequestBody BookTicketRequest request,
            Authentication authentication) {
        String touristId = authentication.getName();
        TicketResponse response = registrationService.bookTickets(id, request, touristId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @GetMapping("/{id}/registrations")
    @PreAuthorize("hasRole('ORGANIZER')")
    @Operation(summary = "List all ticket bookings for an event (Organizer only)")
    public ResponseEntity<PaginatedResponse<TicketResponse>> getEventRegistrations(
            @PathVariable Long id,
            @RequestParam(required = false) RegistrationStatus status,
            @PageableDefault(size = 20) Pageable pageable,
            Authentication authentication) {
        String organizerId = authentication.getName();
        PaginatedResponse<TicketResponse> response = registrationService.getEventRegistrations(id, status, organizerId,
                pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/registrations/{ticketId}")
    @Operation(summary = "Get a specific ticket booking detail")
    public ResponseEntity<ApiResponse<TicketResponse>> getTicket(
            @PathVariable Long id,
            @PathVariable Long ticketId,
            Authentication authentication) {
        String userId = authentication.getName();
        TicketResponse response = registrationService.getTicket(id, ticketId, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}/registrations/{ticketId}")
    @PreAuthorize("hasRole('TOURIST')")
    @Operation(summary = "Cancel a ticket booking")
    public ResponseEntity<ApiResponse<TicketResponse>> cancelBooking(
            @PathVariable Long id,
            @PathVariable Long ticketId,
            @Valid @RequestBody CancelTicketRequest request,
            Authentication authentication) {
        String touristId = authentication.getName();
        TicketResponse response = registrationService.cancelBooking(id, ticketId, request, touristId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/tickets/my")
    @PreAuthorize("hasRole('TOURIST')")
    @Operation(summary = "Get all tickets booked by the logged-in tourist")
    public ResponseEntity<PaginatedResponse<TicketResponse>> getMyTickets(
            @RequestParam(required = false) RegistrationStatus status,
            @PageableDefault(size = 20, sort = "registeredAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable,
            Authentication authentication) {
        String touristId = authentication.getName();
        PaginatedResponse<TicketResponse> response = registrationService.getTouristTickets(touristId, status, pageable);
        return ResponseEntity.ok(response);
    }
}
