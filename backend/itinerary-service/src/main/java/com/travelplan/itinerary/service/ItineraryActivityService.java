package com.travelplan.itinerary.service;

import com.travelplan.itinerary.dto.ItineraryActivityDTO;
import com.travelplan.itinerary.model.ActivityType;
import com.travelplan.itinerary.model.ItineraryActivity;
import com.travelplan.itinerary.model.ItineraryDay;
import com.travelplan.itinerary.repository.ItineraryActivityRepository;
import com.travelplan.itinerary.repository.ItineraryDayRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ItineraryActivityService {
    private final ItineraryActivityRepository activityRepository;
    private final ItineraryDayRepository dayRepository;

    public ItineraryActivityDTO addActivity(Long dayId, ItineraryActivityDTO activityDTO) {
        log.info("Adding activity to day {}", dayId);

        ItineraryDay day = dayRepository.findById(dayId)
                .orElseThrow(() -> new IllegalArgumentException("Day not found"));

        ItineraryActivity activity = ItineraryActivity.builder()
                .day(day)
                .activityType(activityDTO.getActivityType())
                .providerType(activityDTO.getProviderType())
                .providerId(activityDTO.getProviderId())
                .title(activityDTO.getTitle())
                .description(activityDTO.getDescription())
                .startTime(activityDTO.getStartTime())
                .endTime(activityDTO.getEndTime())
                .location(activityDTO.getLocation())
                .estimatedCost(activityDTO.getEstimatedCost())
                .bookingId(activityDTO.getBookingId())
                .sortOrder(activityDTO.getSortOrder() != null ? activityDTO.getSortOrder() : 0)
                .build();

        ItineraryActivity saved = activityRepository.save(activity);
        log.info("Activity created with ID: {}", saved.getId());

        return toDTO(saved);
    }

    public ItineraryActivityDTO updateActivity(Long activityId, ItineraryActivityDTO activityDTO) {
        log.info("Updating activity {}", activityId);

        ItineraryActivity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new IllegalArgumentException("Activity not found"));

        if (activityDTO.getTitle() != null) {
            activity.setTitle(activityDTO.getTitle());
        }
        if (activityDTO.getDescription() != null) {
            activity.setDescription(activityDTO.getDescription());
        }
        if (activityDTO.getStartTime() != null) {
            activity.setStartTime(activityDTO.getStartTime());
        }
        if (activityDTO.getEndTime() != null) {
            activity.setEndTime(activityDTO.getEndTime());
        }
        if (activityDTO.getLocation() != null) {
            activity.setLocation(activityDTO.getLocation());
        }
        if (activityDTO.getSortOrder() != null) {
            activity.setSortOrder(activityDTO.getSortOrder());
        }

        ItineraryActivity updated = activityRepository.save(activity);
        return toDTO(updated);
    }

    public void deleteActivity(Long activityId) {
        log.info("Deleting activity {}", activityId);
        activityRepository.deleteById(activityId);
    }

    public List<ItineraryActivity> getActivitiesForDay(Long dayId) {
        log.info("Fetching activities for day {}", dayId);
        return activityRepository.findByDayIdOrderBySortOrderAscStartTimeAsc(dayId);
    }

    public void addBookingToItinerary(Long itineraryId, Long dayId, String title, LocalTime startTime, LocalTime endTime,
                                     String location, Long bookingId, String providerType, Long providerId, ActivityType activityType) {
        ItineraryDay day = dayRepository.findById(dayId)
                .orElseThrow(() -> new IllegalArgumentException("Day not found"));

        ItineraryActivity activity = ItineraryActivity.builder()
                .day(day)
                .activityType(activityType)
                .title(title)
                .startTime(startTime)
                .endTime(endTime)
                .location(location)
                .bookingId(bookingId)
                .providerType(providerType)
                .providerId(providerId)
                .sortOrder(0)
                .build();

        activityRepository.save(activity);
        log.info("Booking {} added to itinerary {} on day {}", bookingId, itineraryId, dayId);
    }

    public boolean bookingExistsInItinerary(Long itineraryId, Long bookingId) {
        return activityRepository.existsByItineraryIdAndBookingId(itineraryId, bookingId);
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
