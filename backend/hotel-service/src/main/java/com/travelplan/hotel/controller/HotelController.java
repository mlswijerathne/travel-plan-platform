package com.travelplan.hotel.controller;

import com.travelplan.common.dto.ApiResponse;
import com.travelplan.common.dto.PaginatedResponse;
import com.travelplan.hotel.dto.*;
import com.travelplan.hotel.dto.request.CreateHotelRequest;
import com.travelplan.hotel.dto.request.UpdateHotelRequest;
import com.travelplan.hotel.dto.response.AvailabilityResponse;
import com.travelplan.hotel.dto.response.HotelResponse;
import com.travelplan.hotel.service.HotelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/hotels")
@RequiredArgsConstructor
public class HotelController {

    private final HotelService hotelService;

    @PostMapping
    public ResponseEntity<ApiResponse<HotelResponse>> createHotel(
            @Valid @RequestBody CreateHotelRequest request,
            Authentication authentication) {
        String ownerId = authentication.getName();
        log.info("Create hotel request from owner: {}", ownerId);
        HotelResponse response = hotelService.createHotel(ownerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ApiResponse<HotelResponse> getHotelById(@PathVariable Long id) {
        log.debug("Get hotel by id: {}", id);
        return ApiResponse.success(hotelService.getHotelById(id));
    }

    @GetMapping("/{id}/details")
    public ApiResponse<HotelResponse> getHotelByIdWithRooms(@PathVariable Long id) {
        log.debug("Get hotel with rooms by id: {}", id);
        return ApiResponse.success(hotelService.getHotelByIdWithRooms(id));
    }

    @GetMapping
    public PaginatedResponse<HotelResponse> getAllHotels(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.debug("Get all hotels - page: {}, size: {}", page, size);
        return hotelService.getAllHotels(page, size);
    }

    @GetMapping("/owner")
    public PaginatedResponse<HotelResponse> getHotelsByOwner(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        String ownerId = authentication.getName();
        log.debug("Get hotels by owner: {}", ownerId);
        return hotelService.getHotelsByOwner(ownerId, page, size);
    }

    @GetMapping("/search")
    public PaginatedResponse<HotelResponse> searchHotels(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Integer starRating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.debug("Search hotels - city: {}, starRating: {}", city, starRating);
        return hotelService.searchHotels(city, starRating, page, size);
    }

    @GetMapping("/query")
    public PaginatedResponse<HotelResponse> searchHotelsByQuery(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.debug("Search hotels by query: {}", q);
        return hotelService.searchHotelsByQuery(q, page, size);
    }

    @PutMapping("/{id}")
    public ApiResponse<HotelResponse> updateHotel(
            @PathVariable Long id,
            @Valid @RequestBody UpdateHotelRequest request,
            Authentication authentication) {
        String ownerId = authentication.getName();
        log.info("Update hotel request: id={}, owner={}", id, ownerId);
        return ApiResponse.success(hotelService.updateHotel(id, ownerId, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHotel(
            @PathVariable Long id,
            Authentication authentication) {
        String ownerId = authentication.getName();
        log.info("Delete hotel request: id={}, owner={}", id, ownerId);
        hotelService.deleteHotel(id, ownerId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/availability")
    public ApiResponse<AvailabilityResponse> checkAvailability(
            @PathVariable Long id,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        log.debug("Check availability for hotel: {}, dates: {} to {}", id, startDate, endDate);
        return ApiResponse.success(hotelService.checkAvailability(id, startDate, endDate));
    }
}
