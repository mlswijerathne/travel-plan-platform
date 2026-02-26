package com.travelplan.itinerary.controller;

import com.travelplan.itinerary.dto.ItineraryActivityDTO;
import com.travelplan.itinerary.model.ItineraryActivity;
import com.travelplan.itinerary.service.ItineraryActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/itineraries/{itineraryId}/activities")
@RequiredArgsConstructor
@Slf4j
public class ItineraryActivityController {
    private final ItineraryActivityService activityService;

    @GetMapping("/day/{dayId}")
    public ResponseEntity<List<ItineraryActivityDTO>> getActivitiesForDay(
            @PathVariable Long itineraryId,
            @PathVariable Long dayId) {
        log.info("GET activities for day {}", dayId);
        List<ItineraryActivity> activities = activityService.getActivitiesForDay(dayId);
        List<ItineraryActivityDTO> dtos = activities.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/day/{dayId}")
    public ResponseEntity<ItineraryActivityDTO> addActivity(
            @PathVariable Long itineraryId,
            @PathVariable Long dayId,
            @RequestBody ItineraryActivityDTO activityDTO) {
        log.info("POST activity for day {}", dayId);
        ItineraryActivityDTO activity = activityService.addActivity(dayId, activityDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(activity);
    }

    @PutMapping("/{activityId}")
    public ResponseEntity<ItineraryActivityDTO> updateActivity(
            @PathVariable Long itineraryId,
            @PathVariable Long activityId,
            @RequestBody ItineraryActivityDTO activityDTO) {
        log.info("PUT activity {}", activityId);
        ItineraryActivityDTO activity = activityService.updateActivity(activityId, activityDTO);
        return ResponseEntity.ok(activity);
    }

    @DeleteMapping("/{activityId}")
    public ResponseEntity<Void> deleteActivity(
            @PathVariable Long itineraryId,
            @PathVariable Long activityId) {
        log.info("DELETE activity {}", activityId);
        activityService.deleteActivity(activityId);
        return ResponseEntity.noContent().build();
    }

    private ItineraryActivityDTO toDTO(ItineraryActivity activity) {
        return ItineraryActivityDTO.builder()
                .id(activity.getId())
                .activityType(activity.getActivityType())
                .providerType(activity.getProviderType())
                .providerId(activity.getProviderId())
                .title(activity.getTitle())
                .description(activity.getDescription())
                .startTime(activity.getStartTime())
                .endTime(activity.getEndTime())
                .location(activity.getLocation())
                .estimatedCost(activity.getEstimatedCost())
                .bookingId(activity.getBookingId())
                .sortOrder(activity.getSortOrder())
                .build();
    }
}
