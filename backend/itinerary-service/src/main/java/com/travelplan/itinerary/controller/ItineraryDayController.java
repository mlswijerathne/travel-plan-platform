package com.travelplan.itinerary.controller;

import com.travelplan.itinerary.dto.ItineraryDayDTO;
import com.travelplan.itinerary.model.ItineraryDay;
import com.travelplan.itinerary.service.ItineraryDayService;
import com.travelplan.itinerary.service.ItineraryMapperService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/itineraries/{itineraryId}/days")
@RequiredArgsConstructor
@Slf4j
public class ItineraryDayController {
    private final ItineraryDayService dayService;
    private final ItineraryMapperService mapperService;

    @GetMapping
    public ResponseEntity<List<ItineraryDayDTO>> getDays(@PathVariable Long itineraryId) {
        log.info("GET days for itinerary {}", itineraryId);
        List<ItineraryDay> days = dayService.getDaysForItinerary(itineraryId);
        List<ItineraryDayDTO> dtos = days.stream()
                .map(mapperService::toDayDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{dayId}")
    public ResponseEntity<ItineraryDayDTO> getDay(
            @PathVariable Long itineraryId,
            @PathVariable Long dayId) {
        log.info("GET day {} for itinerary {}", dayId, itineraryId);
        ItineraryDay day = dayService.getDaysForItinerary(itineraryId).stream()
                .filter(d -> d.getId().equals(dayId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Day not found"));
        return ResponseEntity.ok(mapperService.toDayDTO(day));
    }

    @PutMapping("/{dayId}")
    public ResponseEntity<ItineraryDayDTO> updateDay(
            @PathVariable Long itineraryId,
            @PathVariable Long dayId,
            @RequestBody UpdateDayRequest request) {
        log.info("PUT day {} for itinerary {}", dayId, itineraryId);
        ItineraryDay day = dayService.updateDay(dayId, request.getNotes());
        return ResponseEntity.ok(mapperService.toDayDTO(day));
    }

    @PostMapping("/generate")
    public ResponseEntity<List<ItineraryDayDTO>> generateDays(@PathVariable Long itineraryId) {
        log.info("POST generate days for itinerary {}", itineraryId);
        List<ItineraryDay> days = dayService.generateDaysForItinerary(itineraryId);
        List<ItineraryDayDTO> dtos = days.stream()
                .map(mapperService::toDayDTO)
                .collect(Collectors.toList());
        return ResponseEntity.status(HttpStatus.CREATED).body(dtos);
    }
}
