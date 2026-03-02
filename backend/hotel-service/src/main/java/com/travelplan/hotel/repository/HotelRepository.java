package com.travelplan.hotel.repository;

import com.travelplan.hotel.entity.Hotel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {

    Page<Hotel> findByIsActiveTrue(Pageable pageable);

    Page<Hotel> findByOwnerId(String ownerId, Pageable pageable);

    Page<Hotel> findByOwnerIdAndIsActiveTrue(String ownerId, Pageable pageable);

    Page<Hotel> findByCity(String city, Pageable pageable);

    Page<Hotel> findByCityAndIsActiveTrue(String city, Pageable pageable);

    Optional<Hotel> findByIdAndOwnerId(Long id, String ownerId);

    @Query("SELECT h FROM Hotel h WHERE h.isActive = true AND " +
           "(:city IS NULL OR h.city = :city) AND " +
           "(:starRating IS NULL OR h.starRating >= :starRating)")
    Page<Hotel> searchHotels(
            @Param("city") String city,
            @Param("starRating") Integer starRating,
            Pageable pageable
    );

    @Query("SELECT h FROM Hotel h WHERE h.isActive = true AND " +
           "LOWER(h.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(h.city) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(h.description) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Hotel> searchByQuery(@Param("query") String query, Pageable pageable);
}
