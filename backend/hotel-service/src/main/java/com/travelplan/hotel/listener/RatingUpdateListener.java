package com.travelplan.hotel.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelplan.common.dto.RatingUpdateEvent;
import com.travelplan.hotel.service.HotelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RatingUpdateListener {

    private final HotelService hotelService;
    private final ObjectMapper objectMapper;

    public void handleRatingUpdate(String message) {
        try {
            log.info("Received rating update event: {}", message);
            RatingUpdateEvent event = objectMapper.readValue(message, RatingUpdateEvent.class);
            
            if ("HOTEL".equalsIgnoreCase(event.getPayload().getEntityType())) {
                Long hotelId = event.getPayload().getEntityId();
                hotelService.updateHotelRating(
                        hotelId,
                        event.getPayload().getNewRating(),
                        event.getPayload().getReviewCount()
                );
                log.info("Hotel rating updated successfully for hotelId: {}", hotelId);
            }
        } catch (Exception e) {
            log.error("Error processing rating update event", e);
        }
    }
}
