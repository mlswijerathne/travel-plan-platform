package com.travelplan.hotel.mapper;

import com.travelplan.hotel.dto.*;
import com.travelplan.hotel.dto.request.CreateHotelRequest;
import com.travelplan.hotel.dto.request.UpdateHotelRequest;
import com.travelplan.hotel.dto.response.HotelResponse;
import com.travelplan.hotel.dto.response.RoomResponse;
import com.travelplan.hotel.entity.Hotel;
import com.travelplan.hotel.entity.Room;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class HotelMapper {

    public Hotel toEntity(CreateHotelRequest request, String ownerId) {
        return Hotel.builder()
                .ownerId(ownerId)
                .name(request.getName())
                .description(request.getDescription())
                .address(request.getAddress())
                .city(request.getCity())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .starRating(request.getStarRating())
                .amenities(request.getAmenities() != null ?
                        request.getAmenities().toArray(new String[0]) : new String[0])
                .checkInTime(request.getCheckInTime())
                .checkOutTime(request.getCheckOutTime())
                .build();
    }

    public HotelResponse toResponse(Hotel hotel) {
        return HotelResponse.builder()
                .id(hotel.getId())
                .ownerId(hotel.getOwnerId())
                .name(hotel.getName())
                .description(hotel.getDescription())
                .address(hotel.getAddress())
                .city(hotel.getCity())
                .latitude(hotel.getLatitude())
                .longitude(hotel.getLongitude())
                .starRating(hotel.getStarRating())
                .averageRating(hotel.getAverageRating())
                .reviewCount(hotel.getReviewCount())
                .amenities(hotel.getAmenities() != null ?
                        Arrays.asList(hotel.getAmenities()) : Collections.emptyList())
                .checkInTime(hotel.getCheckInTime())
                .checkOutTime(hotel.getCheckOutTime())
                .isActive(hotel.getIsActive())
                .createdAt(hotel.getCreatedAt())
                .updatedAt(hotel.getUpdatedAt())
                .build();
    }

    public HotelResponse toResponseWithRooms(Hotel hotel, List<RoomResponse> rooms) {
        HotelResponse response = toResponse(hotel);
        response.setRooms(rooms);
        return response;
    }

    public void updateEntityFromRequest(Hotel hotel, UpdateHotelRequest request) {
        if (request.getName() != null) {
            hotel.setName(request.getName());
        }
        if (request.getDescription() != null) {
            hotel.setDescription(request.getDescription());
        }
        if (request.getAddress() != null) {
            hotel.setAddress(request.getAddress());
        }
        if (request.getCity() != null) {
            hotel.setCity(request.getCity());
        }
        if (request.getLatitude() != null) {
            hotel.setLatitude(request.getLatitude());
        }
        if (request.getLongitude() != null) {
            hotel.setLongitude(request.getLongitude());
        }
        if (request.getStarRating() != null) {
            hotel.setStarRating(request.getStarRating());
        }
        if (request.getAmenities() != null) {
            hotel.setAmenities(request.getAmenities().toArray(new String[0]));
        }
        if (request.getCheckInTime() != null) {
            hotel.setCheckInTime(request.getCheckInTime());
        }
        if (request.getCheckOutTime() != null) {
            hotel.setCheckOutTime(request.getCheckOutTime());
        }
        if (request.getIsActive() != null) {
            hotel.setIsActive(request.getIsActive());
        }
    }
}
