package com.travelplan.hotel.controller;

import com.travelplan.common.dto.ApiResponse;
import com.travelplan.common.dto.PaginatedResponse;
import com.travelplan.hotel.dto.request.CreateRoomRequest;
import com.travelplan.hotel.dto.request.UpdateRoomRequest;
import com.travelplan.hotel.dto.response.RoomResponse;
import com.travelplan.hotel.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @PostMapping
    public ResponseEntity<ApiResponse<RoomResponse>> createRoom(
            @Valid @RequestBody CreateRoomRequest request,
            Authentication authentication) {
        String ownerId = authentication.getName();
        log.info("Create room request from owner: {}", ownerId);
        RoomResponse response = roomService.createRoom(ownerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ApiResponse<RoomResponse> getRoomById(@PathVariable Long id) {
        log.debug("Get room by id: {}", id);
        return ApiResponse.success(roomService.getRoomById(id));
    }

    @GetMapping("/hotel/{hotelId}")
    public ApiResponse<List<RoomResponse>> getRoomsByHotelId(@PathVariable Long hotelId) {
        log.debug("Get rooms by hotel id: {}", hotelId);
        return ApiResponse.success(roomService.getRoomsByHotelId(hotelId));
    }

    @GetMapping("/hotel/{hotelId}/paginated")
    public PaginatedResponse<RoomResponse> getRoomsByHotelIdPaginated(
            @PathVariable Long hotelId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.debug("Get rooms by hotel id paginated: {}", hotelId);
        return roomService.getRoomsByHotelIdPaginated(hotelId, page, size);
    }

    @PutMapping("/{id}")
    public ApiResponse<RoomResponse> updateRoom(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRoomRequest request,
            Authentication authentication) {
        String ownerId = authentication.getName();
        log.info("Update room request: id={}, owner={}", id, ownerId);
        return ApiResponse.success(roomService.updateRoom(id, ownerId, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(
            @PathVariable Long id,
            Authentication authentication) {
        String ownerId = authentication.getName();
        log.info("Delete room request: id={}, owner={}", id, ownerId);
        roomService.deleteRoom(id, ownerId);
        return ResponseEntity.noContent().build();
    }
}
