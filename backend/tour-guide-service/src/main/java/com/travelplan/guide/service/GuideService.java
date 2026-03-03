package com.travelplan.guide.service;

import com.travelplan.guide.dto.*;
import org.springframework.data.domain.Page;
import java.util.List;

public interface GuideService {
    GuideResponse registerGuide(String userId, GuideRequest request);

    GuideResponse getGuide(Long id);

    GuideResponse getGuideByUserId(String userId);

    GuideResponse updateGuide(Long id, GuideRequest request);

    GuideResponse updateGuide(Long id, GuideUpdateRequest request);

    GuideResponse updateGuideByUserId(String userId, GuideRequest request);

    GuideResponse updateGuideByUserId(String userId, GuideUpdateRequest request);

    void deleteGuide(Long id);

    Page<GuideResponse> searchGuides(
            String language,
            String specialization,
            java.math.BigDecimal minRating,
            java.math.BigDecimal maxHourlyRate,
            java.math.BigDecimal maxDailyRate,
            Boolean isVerified,
            String query,
            Integer page,
            Integer size);

    AvailabilityResponse getAvailability(Long id, java.time.LocalDate startDate, java.time.LocalDate endDate);
}