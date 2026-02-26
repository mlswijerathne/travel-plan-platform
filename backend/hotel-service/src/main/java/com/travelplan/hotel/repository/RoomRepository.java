package com.travelplan.hotel.repository;

import com.travelplan.hotel.entity.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    List<Room> findByHotelId(Long hotelId);

    Page<Room> findByHotelId(Long hotelId, Pageable pageable);

    List<Room> findByHotelIdAndIsActiveTrue(Long hotelId);

    Optional<Room> findByIdAndHotelId(Long id, Long hotelId);

    @Query("SELECT r FROM Room r WHERE r.hotel.id = :hotelId AND r.isActive = true")
    List<Room> findActiveRoomsByHotelId(@Param("hotelId") Long hotelId);

    @Query("SELECT COUNT(r) FROM Room r WHERE r.hotel.id = :hotelId AND r.isActive = true")
    long countActiveRoomsByHotelId(@Param("hotelId") Long hotelId);
}
