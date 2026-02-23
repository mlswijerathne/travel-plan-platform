package com.travelplan.event.repository;

import com.travelplan.event.model.entity.Event;
import com.travelplan.event.model.enums.EventStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

    /**
     * Finds all events created by a specific organizer.
     */
    Page<Event> findByOrganizerId(String organizerId, Pageable pageable);

    /**
     * Finds events by organizer and status.
     */
    Page<Event> findByOrganizerIdAndStatus(String organizerId, EventStatus status, Pageable pageable);

    /**
     * Finds a specific event by ID and ensure it exists.
     */
    Optional<Event> findById(Long id);
}
