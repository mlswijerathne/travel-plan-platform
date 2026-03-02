package com.travelplan.event.repository;

import com.travelplan.event.model.entity.TicketTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketTierRepository extends JpaRepository<TicketTier, Long> {
    List<TicketTier> findByEventId(Long eventId);
}
