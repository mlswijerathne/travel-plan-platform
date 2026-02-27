package com.travelplan.hotel.service;

import com.travelplan.hotel.dto.request.CreateHotelRequest;
import com.travelplan.hotel.dto.request.UpdateHotelRequest;
import com.travelplan.hotel.dto.response.AvailabilityResponse;
import com.travelplan.hotel.dto.response.HotelResponse;
import com.travelplan.common.dto.PaginatedResponse;

public interface HotelService {

    HotelResponse createHotel(String ownerId, CreateHotelRequest request);

    HotelResponse getHotelById(Long id);

    HotelResponse getHotelByIdWithRooms(Long id);

    PaginatedResponse<HotelResponse> getAllHotels(int page, int size);

    PaginatedResponse<HotelResponse> getHotelsByOwner(String ownerId, int page, int size);

    PaginatedResponse<HotelResponse> searchHotels(String city, Integer starRating, int page, int size);

    PaginatedResponse<HotelResponse> searchHotelsByQuery(String query, int page, int size);

    HotelResponse updateHotel(Long id, String ownerId, UpdateHotelRequest request);

    void deleteHotel(Long id, String ownerId);

    AvailabilityResponse checkAvailability(Long hotelId, String startDate, String endDate);

    void updateHotelRating(Long hotelId, java.math.BigDecimal newRating, int reviewCount);
}
