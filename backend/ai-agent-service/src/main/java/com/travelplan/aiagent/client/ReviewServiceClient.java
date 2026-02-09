package com.travelplan.aiagent.client;

import com.travelplan.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "review-service", url = "${services.review-url}")
public interface ReviewServiceClient {

    @GetMapping("/api/reviews")
    ApiResponse<Object> getReviews(
            @RequestParam String entityType,
            @RequestParam String entityId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size);

    @GetMapping("/api/reviews/summary")
    ApiResponse<Object> getReviewSummary(
            @RequestParam String entityType,
            @RequestParam String entityId);
}
