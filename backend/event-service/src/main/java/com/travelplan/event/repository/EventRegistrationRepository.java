package com.travelplan.event.repository;

import com.travelplan.event.model.entity.EventRegistration;
import com.travelplan.event.model.enums.RegistrationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {

    /**
     * Finds all registrations for a specific event.
     */
    Page<EventRegistration> findByEventId(Long eventId, Pageable pageable);

    /**
     * Finds all registrations for a specific event (unpaginated).
     */
    java.util.List<EventRegistration> findByEventId(Long eventId);

    /**
     * Finds all registrations for a specific event and status.
     */
    Page<EventRegistration> findByEventIdAndStatus(Long eventId, RegistrationStatus status, Pageable pageable);

    /**
     * Finds all registrations for a specific tourist.
     */
    Page<EventRegistration> findByTouristId(String touristId, Pageable pageable);

    /**
     * Finds all registrations for a specific tourist and status.
     */
    Page<EventRegistration> findByTouristIdAndStatus(String touristId, RegistrationStatus status, Pageable pageable);

    /**
     * Finds a registration by its unique ticket number.
     */
    Optional<EventRegistration> findByTicketNumber(String ticketNumber);
}
