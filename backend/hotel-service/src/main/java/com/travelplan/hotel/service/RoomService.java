package com.travelplan.hotel.service;

import com.travelplan.hotel.dto.request.CreateRoomRequest;
import com.travelplan.hotel.dto.request.UpdateRoomRequest;
import com.travelplan.hotel.dto.response.RoomResponse;
import com.travelplan.common.dto.PaginatedResponse;

import java.util.List;

public interface RoomService {

    RoomResponse createRoom(String ownerId, CreateRoomRequest request);

    RoomResponse getRoomById(Long id);

    List<RoomResponse> getRoomsByHotelId(Long hotelId);

    PaginatedResponse<RoomResponse> getRoomsByHotelIdPaginated(Long hotelId, int page, int size);

    RoomResponse updateRoom(Long id, String ownerId, UpdateRoomRequest request);

    void deleteRoom(Long id, String ownerId);
}
