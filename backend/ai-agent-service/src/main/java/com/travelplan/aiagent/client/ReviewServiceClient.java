package com.travelplan.aiagent.client;

import com.travelplan.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "review-service", url = "${feign.review-service.url:}")
public interface ReviewServiceClient {

    @GetMapping("/api/reviews/entity/{entityType}/{entityId}")
    ApiResponse<Object> getReviews(
            @PathVariable String entityType,
            @PathVariable String entityId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size);

    @GetMapping("/api/reviews/summary/{entityType}/{entityId}")
    ApiResponse<Object> getReviewSummary(
            @PathVariable String entityType,
            @PathVariable String entityId);
}
