package com.travelplan.guide.controller;

import com.travelplan.common.dto.ApiResponse;
import com.travelplan.common.dto.PaginatedResponse;
import com.travelplan.guide.dto.AvailabilityResponse;
import com.travelplan.guide.dto.GuideRequest;
import com.travelplan.guide.dto.GuideResponse;
import com.travelplan.guide.dto.GuideUpdateRequest;
import com.travelplan.guide.service.GuideService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/tour-guides")
public class GuideController {

    private final GuideService guideService;

    public GuideController(GuideService guideService) {
        this.guideService = guideService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<GuideResponse>> registerGuide(@Valid @RequestBody GuideRequest request) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        if (userId == null || "anonymousUser".equals(userId)) {
            // Generate a random UUID to prevent unique constraint violations in testing
            userId = java.util.UUID.randomUUID().toString();
        }

        GuideResponse response = guideService.registerGuide(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GuideResponse>> getGuide(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(guideService.getGuide(id)));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<GuideResponse>> getMyProfile() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        if ("anonymousUser".equals(userId)) {
            userId = "mock-postman-user-id";
        }
        return ResponseEntity.ok(ApiResponse.success(guideService.getGuideByUserId(userId)));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<GuideResponse>> updateMyProfile(@RequestBody GuideUpdateRequest request) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        if ("anonymousUser".equals(userId)) {
            userId = "mock-postman-user-id";
        }
        return ResponseEntity.ok(ApiResponse.success(guideService.updateGuideByUserId(userId, request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<GuideResponse>> updateGuide(@PathVariable Long id,
            @RequestBody GuideUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(guideService.updateGuide(id, request)));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivateGuide(@PathVariable Long id) {
        guideService.deleteGuide(id);
    }

    @GetMapping
    public ResponseEntity<PaginatedResponse<GuideResponse>> searchGuides(
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String specialization,
            @RequestParam(required = false) BigDecimal minRating,
            @RequestParam(required = false) BigDecimal maxHourlyRate,
            @RequestParam(required = false) BigDecimal maxDailyRate,
            @RequestParam(required = false) Boolean isVerified,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        Page<GuideResponse> guidesPage = guideService.searchGuides(
                language, specialization, minRating, maxHourlyRate, maxDailyRate, isVerified, query, page, size);

        return ResponseEntity.ok(PaginatedResponse.of(
                guidesPage.getContent(),
                page,
                size,
                guidesPage.getTotalElements()));
    }

    @GetMapping("/{id}/availability")
    public ResponseEntity<ApiResponse<AvailabilityResponse>> getAvailability(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        return ResponseEntity.ok(ApiResponse.success(guideService.getAvailability(id, startDate, endDate)));
    }
}