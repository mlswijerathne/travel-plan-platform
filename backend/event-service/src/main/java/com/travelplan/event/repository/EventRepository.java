package com.travelplan.event.repository;

import com.travelplan.event.model.entity.Event;
import com.travelplan.event.model.enums.EventStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

    /**
     * Finds all events created by a specific organizer.
     */
    Page<Event> findByOrganizerId(String organizerId, Pageable pageable);

    /**
     * Finds events by organizer and status.
     */
    Page<Event> findByOrganizerIdAndStatus(String organizerId, EventStatus status, Pageable pageable);

    /**
     * Finds a specific event by ID and ensure it exists.
     */
    Optional<Event> findById(Long id);

    // -------------------------------------------------------------------------
    // EPIC-7: Geospatial queries
    // -------------------------------------------------------------------------

    /**
     * TASK-7.1 (Route pre-filter): Returns PUBLISHED events with coordinates that fall
     * inside a rectangular bounding box. Date-range filtering is applied in the
     * service layer to avoid null-parameter type-inference issues in PostgreSQL.
     */
    @Query("""
            SELECT e FROM Event e
            WHERE e.status = 'PUBLISHED'
              AND e.latitude  IS NOT NULL
              AND e.longitude IS NOT NULL
              AND e.latitude  BETWEEN :minLat AND :maxLat
              AND e.longitude BETWEEN :minLng AND :maxLng
            ORDER BY e.startDateTime ASC
            """)
    List<Event> findPublishedInBoundingBox(
            @Param("minLat") double minLat,
            @Param("maxLat") double maxLat,
            @Param("minLng") double minLng,
            @Param("maxLng") double maxLng);

    /**
     * TASK-7.2 (Regional Pulse): Returns PUBLISHED events whose location is within
     * {@code radiusKm} kilometres of the given centre point, using the Haversine
     * formula evaluated in PostgreSQL. Ordered by start_date_time ascending.
     *
     * SQL Haversine:
     *   distance = 6371 * acos( LEAST(1.0,
     *       cos(radians(lat)) * cos(radians(e.latitude)) * cos(radians(e.longitude) - radians(lng))
     *       + sin(radians(lat)) * sin(radians(e.latitude))
     *   ))
     *
     * LEAST(1.0, ...) guards against acos domain errors from floating-point noise.
     */
    @Query(value = """
            SELECT e.* FROM events e
            WHERE e.status = 'PUBLISHED'
              AND e.latitude  IS NOT NULL
              AND e.longitude IS NOT NULL
              AND (
                  6371.0 * acos( LEAST(1.0,
                      cos(radians(:lat))  * cos(radians(e.latitude))
                                          * cos(radians(e.longitude) - radians(:lng))
                      + sin(radians(:lat)) * sin(radians(e.latitude))
                  ))
              ) <= :radiusKm
              AND e.start_date_time >= NOW()
            ORDER BY e.start_date_time ASC
            """,
            nativeQuery = true)
    List<Event> findPublishedWithinRadiusKm(
            @Param("lat")      double lat,
            @Param("lng")      double lng,
            @Param("radiusKm") double radiusKm);
}

