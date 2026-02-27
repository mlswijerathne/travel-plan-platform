package com.travelplan.itinerary.repository;

import com.travelplan.itinerary.model.ItineraryDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ItineraryDayRepository extends JpaRepository<ItineraryDay, Long> {
    List<ItineraryDay> findByItineraryId(Long itineraryId);

    @Query("SELECT d.id FROM ItineraryDay d WHERE d.itinerary.id = :itineraryId ORDER BY d.dayNumber ASC")
    List<Long> findIdsByItineraryId(@Param("itineraryId") Long itineraryId);

    Optional<ItineraryDay> findByItineraryIdAndDate(Long itineraryId, LocalDate date);

    List<ItineraryDay> findByItineraryIdOrderByDayNumberAsc(Long itineraryId);
}
