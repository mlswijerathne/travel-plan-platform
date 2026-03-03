package com.travelplan.hotel.mapper;

import com.travelplan.hotel.dto.request.CreateRoomRequest;
import com.travelplan.hotel.dto.request.InlineRoomRequest;
import com.travelplan.hotel.dto.request.UpdateRoomRequest;
import com.travelplan.hotel.dto.response.RoomResponse;
import com.travelplan.hotel.entity.Hotel;
import com.travelplan.hotel.entity.Room;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class RoomMapper {

    public Room toEntity(CreateRoomRequest request, Hotel hotel) {
        return Room.builder()
                .hotel(hotel)
                .roomType(request.getRoomType())
                .name(request.getName())
                .description(request.getDescription())
                .pricePerNight(request.getPricePerNight())
                .maxOccupancy(request.getMaxOccupancy() != null ? request.getMaxOccupancy() : 2)
                .amenities(request.getAmenities() != null ?
                        request.getAmenities().toArray(new String[0]) : new String[0])
                .totalRooms(request.getTotalRooms() != null ? request.getTotalRooms() : 1)
                .build();
    }

    public Room toEntity(InlineRoomRequest request, Hotel hotel) {
        return Room.builder()
                .hotel(hotel)
                .roomType(request.getRoomType())
                .name(request.getName())
                .description(request.getDescription())
                .pricePerNight(request.getPricePerNight())
                .maxOccupancy(request.getMaxOccupancy() != null ? request.getMaxOccupancy() : 2)
                .amenities(request.getAmenities() != null ?
                        request.getAmenities().toArray(new String[0]) : new String[0])
                .totalRooms(request.getTotalRooms() != null ? request.getTotalRooms() : 1)
                .build();
    }

    public RoomResponse toResponse(Room room) {
        return RoomResponse.builder()
                .id(room.getId())
                .hotelId(room.getHotel().getId())
                .roomType(room.getRoomType())
                .name(room.getName())
                .description(room.getDescription())
                .pricePerNight(room.getPricePerNight())
                .maxOccupancy(room.getMaxOccupancy())
                .amenities(room.getAmenities() != null ?
                        Arrays.asList(room.getAmenities()) : Collections.emptyList())
                .totalRooms(room.getTotalRooms())
                .isActive(room.getIsActive())
                .createdAt(room.getCreatedAt())
                .updatedAt(room.getUpdatedAt())
                .build();
    }

    public List<RoomResponse> toResponseList(List<Room> rooms) {
        return rooms.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public void updateEntityFromRequest(Room room, UpdateRoomRequest request) {
        if (request.getRoomType() != null) {
            room.setRoomType(request.getRoomType());
        }
        if (request.getName() != null) {
            room.setName(request.getName());
        }
        if (request.getDescription() != null) {
            room.setDescription(request.getDescription());
        }
        if (request.getPricePerNight() != null) {
            room.setPricePerNight(request.getPricePerNight());
        }
        if (request.getMaxOccupancy() != null) {
            room.setMaxOccupancy(request.getMaxOccupancy());
        }
        if (request.getAmenities() != null) {
            room.setAmenities(request.getAmenities().toArray(new String[0]));
        }
        if (request.getTotalRooms() != null) {
            room.setTotalRooms(request.getTotalRooms());
        }
        if (request.getIsActive() != null) {
            room.setIsActive(request.getIsActive());
        }
    }
}
