package com.travelplan.vehicle.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelplan.common.dto.RatingUpdateEvent;
import com.travelplan.vehicle.entity.Vehicle;
import com.travelplan.vehicle.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RatingUpdateListener {

    private final VehicleRepository vehicleRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "rating-update-events", groupId = "vehicle-service-group")
    public void handleRatingUpdate(String message) {
        try {
            log.info("Received rating update event: {}", message);
            RatingUpdateEvent event = objectMapper.readValue(message, RatingUpdateEvent.class);

            if (!"VEHICLE".equalsIgnoreCase(event.getPayload().getEntityType())) {
                return;
            }

            Long vehicleId = event.getPayload().getEntityId();
            vehicleRepository.findById(vehicleId).ifPresent(vehicle -> {
                vehicle.setAverageRating(event.getPayload().getNewRating());
                vehicle.setReviewCount(event.getPayload().getReviewCount());
                vehicleRepository.save(vehicle);
                log.info("Vehicle rating updated for vehicleId: {}", vehicleId);
            });
        } catch (Exception e) {
            log.error("Error processing rating update event", e);
            throw new RuntimeException("Failed to process rating update event", e);
        }
    }
}
