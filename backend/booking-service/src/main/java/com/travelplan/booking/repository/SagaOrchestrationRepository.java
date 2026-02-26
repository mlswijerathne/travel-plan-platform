package com.travelplan.booking.repository;

import com.travelplan.booking.entity.SagaOrchestration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SagaOrchestrationRepository extends JpaRepository<SagaOrchestration, Long> {

    Optional<SagaOrchestration> findByBookingId(Long bookingId);
}
