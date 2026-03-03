package com.travelplan.itinerary.controller;

import com.travelplan.itinerary.dto.ItineraryDTO;
import com.travelplan.itinerary.dto.MapDataDTO;
import com.travelplan.itinerary.service.ItineraryService;
import com.travelplan.itinerary.service.MapVisualizationService;
import com.travelplan.itinerary.service.PdfGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/itineraries")
@RequiredArgsConstructor
@Slf4j
public class ItineraryController {
    private final ItineraryService itineraryService;
    private final PdfGenerationService pdfGenerationService;
    private final MapVisualizationService mapVisualizationService;

    @GetMapping
    public ResponseEntity<List<ItineraryDTO>> getItineraries(Authentication authentication) {
        String touristId = authentication.getName();
        log.info("GET /api/v1/itineraries for tourist {}", touristId);
        List<ItineraryDTO> itineraries = itineraryService.getItinerariesByTourist(touristId);
        return ResponseEntity.ok(itineraries);
    }

    @GetMapping("/active")
    public ResponseEntity<List<ItineraryDTO>> getActiveItineraries(Authentication authentication) {
        String touristId = authentication.getName();
        log.info("GET /api/v1/itineraries/active for tourist {}", touristId);
        List<ItineraryDTO> itineraries = itineraryService.getActiveItineraries(touristId);
        return ResponseEntity.ok(itineraries);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItineraryDTO> getItinerary(
            @PathVariable Long id,
            Authentication authentication) {
        String touristId = authentication.getName();
        log.info("GET /api/v1/itineraries/{} for tourist {}", id, touristId);
        ItineraryDTO itinerary = itineraryService.getItinerary(id, touristId);
        return ResponseEntity.ok(itinerary);
    }

    @PostMapping
    public ResponseEntity<ItineraryDTO> createItinerary(
            Authentication authentication,
            @RequestBody CreateItineraryRequest request) {
        String touristId = authentication.getName();
        log.info("POST /api/v1/itineraries for tourist {}", touristId);
        ItineraryDTO itinerary = itineraryService.createItinerary(
                touristId,
                request.getTitle(),
                request.getDescription(),
                request.getStartDate(),
                request.getEndDate()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(itinerary);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ItineraryDTO> updateItinerary(
            @PathVariable Long id,
            Authentication authentication,
            @RequestBody ItineraryDTO request) {
        String touristId = authentication.getName();
        log.info("PUT /api/v1/itineraries/{} for tourist {}", id, touristId);
        ItineraryDTO itinerary = itineraryService.updateItinerary(id, touristId, request);
        return ResponseEntity.ok(itinerary);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItinerary(
            @PathVariable Long id,
            Authentication authentication) {
        String touristId = authentication.getName();
        log.info("DELETE /api/v1/itineraries/{} for tourist {}", id, touristId);
        itineraryService.deleteItinerary(id, touristId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<ItineraryDTO> activateItinerary(
            @PathVariable Long id,
            Authentication authentication) {
        String touristId = authentication.getName();
        log.info("POST /api/v1/itineraries/{}/activate for tourist {}", id, touristId);
        ItineraryDTO itinerary = itineraryService.activateItinerary(id, touristId);
        return ResponseEntity.ok(itinerary);
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<ItineraryDTO> completeItinerary(
            @PathVariable Long id,
            Authentication authentication) {
        String touristId = authentication.getName();
        log.info("POST /api/v1/itineraries/{}/complete for tourist {}", id, touristId);
        ItineraryDTO itinerary = itineraryService.completeItinerary(id, touristId);
        return ResponseEntity.ok(itinerary);
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadPDF(
            @PathVariable Long id,
            Authentication authentication) {
        String touristId = authentication.getName();
        log.info("GET /api/v1/itineraries/{}/pdf for tourist {}", id, touristId);

        try {
            byte[] pdfContent = pdfGenerationService.generateItineraryPDF(id, touristId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "itinerary-" + id + ".pdf");
            headers.setContentLength(pdfContent.length);

            return ResponseEntity.ok().headers(headers).body(pdfContent);
        } catch (Exception e) {
            log.error("Error downloading PDF for itinerary {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}/map")
    public ResponseEntity<MapDataDTO> getItineraryMap(
            @PathVariable Long id,
            Authentication authentication) {
        String touristId = authentication.getName();
        log.info("GET /api/v1/itineraries/{}/map for tourist {}", id, touristId);
        MapDataDTO mapData = mapVisualizationService.generateItineraryMapData(id, touristId);
        return ResponseEntity.ok(mapData);
    }

    @GetMapping("/{id}/days/{dayId}/map")
    public ResponseEntity<MapDataDTO> getDayMap(
            @PathVariable Long id,
            @PathVariable Long dayId,
            Authentication authentication) {
        String touristId = authentication.getName();
        log.info("GET /api/v1/itineraries/{}/days/{}/map for tourist {}", id, dayId, touristId);
        MapDataDTO mapData = mapVisualizationService.generateDayMapData(id, dayId, touristId);
        return ResponseEntity.ok(mapData);
    }
}
