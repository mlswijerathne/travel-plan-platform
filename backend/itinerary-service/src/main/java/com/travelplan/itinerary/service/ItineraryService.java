package com.travelplan.itinerary.service;

import com.travelplan.itinerary.dto.ItineraryDTO;
import com.travelplan.itinerary.model.Itinerary;
import com.travelplan.itinerary.model.TripStatus;
import com.travelplan.itinerary.repository.ItineraryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ItineraryService {
    private final ItineraryRepository itineraryRepository;
    private final ItineraryMapperService mapperService;

    public ItineraryDTO createItinerary(String touristId, String title, String description, LocalDate startDate, LocalDate endDate) {
        log.info("Creating new itinerary for tourist: {} from {} to {}", touristId, startDate, endDate);

        Itinerary itinerary = Itinerary.builder()
                .touristId(touristId)
                .title(title)
                .description(description)
                .startDate(startDate)
                .endDate(endDate)
                .status(TripStatus.DRAFT)
                .build();

        Itinerary saved = itineraryRepository.save(itinerary);
        log.info("Itinerary created with ID: {}", saved.getId());

        return mapperService.toDTO(saved);
    }

    public ItineraryDTO getItinerary(Long id, String touristId) {
        log.info("Fetching itinerary {} for tourist {}", id, touristId);
        Itinerary itinerary = itineraryRepository.findByIdAndTouristId(id, touristId)
                .orElseThrow(() -> new IllegalArgumentException("Itinerary not found or access denied"));
        return mapperService.toDTO(itinerary);
    }

    public List<ItineraryDTO> getItinerariesByTourist(String touristId) {
        log.info("Fetching all itineraries for tourist: {}", touristId);
        return itineraryRepository.findByTouristId(touristId).stream()
                .map(mapperService::toDTO)
                .collect(Collectors.toList());
    }

    public List<ItineraryDTO> getActiveItineraries(String touristId) {
        log.info("Fetching active itineraries for tourist: {}", touristId);
        return itineraryRepository.findActiveItinerariesForTourist(touristId, TripStatus.ACTIVE).stream()
                .map(mapperService::toDTO)
                .collect(Collectors.toList());
    }

    public ItineraryDTO updateItinerary(Long id, String touristId, ItineraryDTO dto) {
        log.info("Updating itinerary {}", id);
        Itinerary itinerary = itineraryRepository.findByIdAndTouristId(id, touristId)
                .orElseThrow(() -> new IllegalArgumentException("Itinerary not found or access denied"));

        if (dto.getTitle() != null) {
            itinerary.setTitle(dto.getTitle());
        }
        if (dto.getDescription() != null) {
            itinerary.setDescription(dto.getDescription());
        }
        if (dto.getStatus() != null) {
            itinerary.setStatus(dto.getStatus());
        }
        if (dto.getTotalBudget() != null) {
            itinerary.setTotalBudget(dto.getTotalBudget());
        }

        Itinerary updated = itineraryRepository.save(itinerary);
        return mapperService.toDTO(updated);
    }

    public void deleteItinerary(Long id, String touristId) {
        log.info("Deleting itinerary {}", id);
        Itinerary itinerary = itineraryRepository.findByIdAndTouristId(id, touristId)
                .orElseThrow(() -> new IllegalArgumentException("Itinerary not found or access denied"));
        itineraryRepository.delete(itinerary);
    }

    public ItineraryDTO activateItinerary(Long id, String touristId) {
        log.info("Activating itinerary {}", id);
        Itinerary itinerary = itineraryRepository.findByIdAndTouristId(id, touristId)
                .orElseThrow(() -> new IllegalArgumentException("Itinerary not found or access denied"));

        itinerary.setStatus(TripStatus.ACTIVE);
        Itinerary updated = itineraryRepository.save(itinerary);
        return mapperService.toDTO(updated);
    }

    public ItineraryDTO completeItinerary(Long id, String touristId) {
        log.info("Completing itinerary {}", id);
        Itinerary itinerary = itineraryRepository.findByIdAndTouristId(id, touristId)
                .orElseThrow(() -> new IllegalArgumentException("Itinerary not found or access denied"));

        itinerary.setStatus(TripStatus.COMPLETED);
        Itinerary updated = itineraryRepository.save(itinerary);
        return mapperService.toDTO(updated);
    }

    public List<Itinerary> findCompletedTrips() {
        log.info("Finding completed trips");
        return itineraryRepository.findCompletedTrips(LocalDate.now());
    }

    public Itinerary findOrCreateItinerary(String touristId, LocalDate startDate, LocalDate endDate) {
        List<Itinerary> existing = itineraryRepository.findItinerariesByDateRange(touristId, startDate);
        
        if (!existing.isEmpty()) {
            log.info("Found existing itinerary for tourist {} with overlapping dates", touristId);
            return existing.get(0);
        }

        log.info("Creating new itinerary for overlapping dates");
        Itinerary itinerary = Itinerary.builder()
                .touristId(touristId)
                .title("Trip")
                .description(null)
                .startDate(startDate)
                .endDate(endDate)
                .status(TripStatus.DRAFT)
                .build();
        return itineraryRepository.save(itinerary);
    }
}
