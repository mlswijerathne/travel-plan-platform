package com.travelplan.aiagent.tool;

import com.google.adk.tools.Annotations.Schema;
import com.travelplan.common.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class ReviewTools {

    @Schema(description = "Get reviews and ratings for a specific provider (hotel, tour guide, or vehicle). Returns customer reviews and an overall rating summary.")
    public static Map<String, Object> getProviderReviews(
            @Schema(description = "Type of provider: 'HOTEL', 'TOUR_GUIDE', or 'VEHICLE'") String entityType,
            @Schema(description = "The unique ID of the provider to get reviews for") String entityId) {
        try {
            log.info("Getting reviews - entityType: {}, entityId: {}", entityType, entityId);

            ApiResponse<Object> reviewsResponse = ToolRegistry.getInstance().getReviewServiceClient()
                    .getReviews(entityType, entityId, 0, 5);

            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("reviews", reviewsResponse.getData());

            try {
                ApiResponse<Object> summaryResponse = ToolRegistry.getInstance().getReviewServiceClient()
                        .getReviewSummary(entityType, entityId);
                result.put("summary", summaryResponse.getData());
            } catch (Exception e) {
                log.warn("Could not get review summary for {} {}: {}", entityType, entityId, e.getMessage());
            }

            return result;
        } catch (Exception e) {
            log.error("Error getting reviews: {}", e.getMessage());
            Map<String, Object> result = new HashMap<>();
            result.put("status", "error");
            result.put("message", "Review service is currently unavailable for " + entityType + " " + entityId);
            return result;
        }
    }
}
