package com.travelplan.guide.messaging;

import com.travelplan.guide.domain.Guide;
import com.travelplan.guide.repository.GuideRepository;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.sqs.enabled", havingValue = "true", matchIfMissing = false)
public class RatingUpdateListener {

    private final GuideRepository guideRepository;

    @SqsListener("${app.sqs.rating-update-queue}")
    public void handleRatingUpdate(RatingUpdateEvent event) {
        log.info("Received rating update event for {} ID: {}", event.getEntityType(), event.getEntityId());

        if (!"TOUR_GUIDE".equals(event.getEntityType())) {
            return;
        }

        guideRepository.findById(event.getEntityId()).ifPresent(guide -> {
            guide.setAverageRating(event.getAverageRating());
            guide.setReviewCount(event.getReviewCount());
            guideRepository.save(guide);
            log.info("Updated rating for guide {}: rating={}, count={}",
                    guide.getId(), event.getAverageRating(), event.getReviewCount());
        });
    }

    @Data
    public static class RatingUpdateEvent {
        private Long entityId;
        private String entityType;
        private BigDecimal averageRating;
        private Integer reviewCount;
    }
}
