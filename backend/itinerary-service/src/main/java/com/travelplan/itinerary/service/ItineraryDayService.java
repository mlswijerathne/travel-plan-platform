package com.travelplan.itinerary.service;

import com.travelplan.itinerary.model.Itinerary;
import com.travelplan.itinerary.model.ItineraryDay;
import com.travelplan.itinerary.repository.ItineraryDayRepository;
import com.travelplan.itinerary.repository.ItineraryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ItineraryDayService {
    private final ItineraryDayRepository dayRepository;
    private final ItineraryRepository itineraryRepository;

    public List<ItineraryDay> generateDaysForItinerary(Long itineraryId) {
        log.info("Generating days for itinerary {}", itineraryId);

        Itinerary itinerary = itineraryRepository.findById(itineraryId)
                .orElseThrow(() -> new IllegalArgumentException("Itinerary not found"));

        LocalDate currentDate = itinerary.getStartDate();
        int dayNumber = 1;

        while (!currentDate.isAfter(itinerary.getEndDate())) {
            ItineraryDay day = ItineraryDay.builder()
                    .itinerary(itinerary)
                    .dayNumber(dayNumber)
                    .date(currentDate)
                    .build();

            dayRepository.save(day);
            log.debug("Created day {} for itinerary {}", dayNumber, itineraryId);

            currentDate = currentDate.plusDays(1);
            dayNumber++;
        }

        return dayRepository.findByItineraryIdOrderByDayNumberAsc(itineraryId);
    }

    public ItineraryDay getOrCreateDay(Long itineraryId, LocalDate date) {
        log.info("Getting or creating day for itinerary {} on date {}", itineraryId, date);

        Itinerary itinerary = itineraryRepository.findById(itineraryId)
                .orElseThrow(() -> new IllegalArgumentException("Itinerary not found"));

        return dayRepository.findByItineraryIdAndDate(itineraryId, date)
                .orElseGet(() -> {
                    long daysDiff = java.time.temporal.ChronoUnit.DAYS.between(itinerary.getStartDate(), date);
                    int dayNumber = (int) daysDiff + 1;

                    ItineraryDay day = ItineraryDay.builder()
                            .itinerary(itinerary)
                            .dayNumber(dayNumber)
                            .date(date)
                            .build();

                    return dayRepository.save(day);
                });
    }

    public List<ItineraryDay> getDaysForItinerary(Long itineraryId) {
        return dayRepository.findByItineraryIdOrderByDayNumberAsc(itineraryId);
    }

    public ItineraryDay updateDay(Long dayId, String notes) {
        log.info("Updating day {}", dayId);
        ItineraryDay day = dayRepository.findById(dayId)
                .orElseThrow(() -> new IllegalArgumentException("Day not found"));

        day.setNotes(notes);
        return dayRepository.save(day);
    }
}
