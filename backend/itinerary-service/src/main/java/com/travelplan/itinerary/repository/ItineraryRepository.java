package com.travelplan.itinerary.repository;

import com.travelplan.itinerary.model.Itinerary;
import com.travelplan.itinerary.model.TripStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ItineraryRepository extends JpaRepository<Itinerary, Long> {
    List<Itinerary> findByTouristId(String touristId);

    List<Itinerary> findByTouristIdAndStatus(String touristId, TripStatus status);

    @Query("SELECT i FROM Itinerary i WHERE i.touristId = :touristId AND i.status = :status ORDER BY i.startDate DESC")
    List<Itinerary> findActiveItinerariesForTourist(@Param("touristId") String touristId, @Param("status") TripStatus status);

    @Query("SELECT i FROM Itinerary i WHERE i.status = 'ACTIVE' AND i.endDate < :currentDate")
    List<Itinerary> findCompletedTrips(@Param("currentDate") LocalDate currentDate);

    @Query("SELECT i FROM Itinerary i WHERE i.touristId = :touristId AND i.startDate <= :date AND i.endDate >= :date")
    List<Itinerary> findItinerariesByDateRange(@Param("touristId") String touristId, @Param("date") LocalDate date);

    Optional<Itinerary> findByIdAndTouristId(Long id, String touristId);
}
