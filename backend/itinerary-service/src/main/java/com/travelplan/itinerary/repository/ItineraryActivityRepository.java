package com.travelplan.itinerary.repository;

import com.travelplan.itinerary.model.ItineraryActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItineraryActivityRepository extends JpaRepository<ItineraryActivity, Long> {
    List<ItineraryActivity> findByDayIdOrderBySortOrderAscStartTimeAsc(Long dayId);

    List<ItineraryActivity> findByDayId(Long dayId);

    @Query("SELECT ia FROM ItineraryActivity ia WHERE ia.day.itinerary.id = :itineraryId AND ia.bookingId = :bookingId")
    List<ItineraryActivity> findByItineraryIdAndBookingId(@Param("itineraryId") Long itineraryId, @Param("bookingId") Long bookingId);

    @Query("SELECT CASE WHEN COUNT(ia) > 0 THEN true ELSE false END FROM ItineraryActivity ia WHERE ia.day.itinerary.id = :itineraryId AND ia.bookingId = :bookingId")
    boolean existsByItineraryIdAndBookingId(@Param("itineraryId") Long itineraryId, @Param("bookingId") Long bookingId);
}
