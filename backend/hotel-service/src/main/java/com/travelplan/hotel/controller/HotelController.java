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

    // Dependency injection - Service layer handles business logic
    private final HotelService hotelService;

    /**
     * Create a new hotel
     * 
     * Endpoint: POST /api/hotels
     * Access: Protected (requires authentication)
     * 
     * @param request Hotel creation data (validated)
     * @param authentication JWT authentication token (contains owner ID)
     * @return Created hotel details with HTTP 201 status
     */
    @PostMapping
    public ResponseEntity<ApiResponse<HotelResponse>> createHotel(
            @Valid @RequestBody CreateHotelRequest request,
            Authentication authentication) {
        String ownerId = authentication.getName(); // Extract user ID from JWT token
        log.info("Create hotel request from owner: {}", ownerId);
        HotelResponse response = hotelService.createHotel(ownerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    /**
     * Get hotel by ID (basic information without rooms)
     * 
     * Endpoint: GET /api/hotels/{id}
     * Access: Public
     * 
     * @param id Hotel ID
     * @return Hotel details
     */
    @GetMapping("/{id}")
    public ApiResponse<HotelResponse> getHotelById(@PathVariable Long id) {
        log.debug("Get hotel by id: {}", id);
        return ApiResponse.success(hotelService.getHotelById(id));
    }

    /**
     * Get hotel by ID with all room details
     * 
     * Endpoint: GET /api/hotels/{id}/details
     * Access: Public
     * 
     * @param id Hotel ID
     * @return Hotel details including all room types
     */
    @GetMapping("/{id}/details")
    public ApiResponse<HotelResponse> getHotelByIdWithRooms(@PathVariable Long id) {
        log.debug("Get hotel with rooms by id: {}", id);
        return ApiResponse.success(hotelService.getHotelByIdWithRooms(id));
    }

    /**
     * Get all active hotels (paginated)
     * 
     * Endpoint: GET /api/hotels?page=0&size=20
     * Access: Public
     * 
     * @param page Page number (0-based, default: 0)
     * @param size Items per page (default: 20)
     * @return Paginated list of hotels
     */
    @GetMapping
    public PaginatedResponse<HotelResponse> getAllHotels(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.debug("Get all hotels - page: {}, size: {}", page, size);
        return hotelService.getAllHotels(page, size);
    }

    /**
     * Get hotels owned by the authenticated user
     * 
     * Endpoint: GET /api/hotels/owner?page=0&size=20
     * Access: Protected (requires authentication)
     * 
     * @param page Page number (0-based, default: 0)
     * @param size Items per page (default: 20)
     * @param authentication JWT authentication token
     * @return Paginated list of owner's hotels
     */
    @GetMapping("/owner")
    public PaginatedResponse<HotelResponse> getHotelsByOwner(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        String ownerId = authentication.getName();
        log.debug("Get hotels by owner: {}", ownerId);
        return hotelService.getHotelsByOwner(ownerId, page, size);
    }

    /**
     * Search hotels by city and/or star rating
     * 
     * Endpoint: GET /api/hotels/search?city=Colombo&starRating=4
     * Access: Public
     * 
     * @param city City name (optional)
     * @param starRating Minimum star rating (optional, 1-5)
     * @param page Page number (0-based, default: 0)
     * @param size Items per page (default: 20)
     * @return Paginated list of matching hotels (sorted by rating)
     */
    @GetMapping("/search")
    public PaginatedResponse<HotelResponse> searchHotels(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Integer starRating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.debug("Search hotels - city: {}, starRating: {}", city, starRating);
        return hotelService.searchHotels(city, starRating, page, size);
    }

    /**
     * Search hotels by text query (searches in name, city, description)
     * 
     * Endpoint: GET /api/hotels/query?q=beach hotel
     * Access: Public
     * 
     * @param q Search query string
     * @param page Page number (0-based, default: 0)
     * @param size Items per page (default: 20)
     * @return Paginated list of matching hotels
     */
    @GetMapping("/query")
    public PaginatedResponse<HotelResponse> searchHotelsByQuery(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.debug("Search hotels by query: {}", q);
        return hotelService.searchHotelsByQuery(q, page, size);
    }

    /**
     * Update hotel information
     * 
     * Endpoint: PUT /api/hotels/{id}
     * Access: Protected (only hotel owner can update)
     * 
     * @param id Hotel ID
     * @param request Updated hotel data (validated)
     * @param authentication JWT authentication token
     * @return Updated hotel details
     * @throws ForbiddenException if user is not the owner
     */
    @PutMapping("/{id}")
    public ApiResponse<HotelResponse> updateHotel(
            @PathVariable Long id,
            @Valid @RequestBody UpdateHotelRequest request,
            Authentication authentication) {
        String ownerId = authentication.getName();
        log.info("Update hotel request: id={}, owner={}", id, ownerId);
        return ApiResponse.success(hotelService.updateHotel(id, ownerId, request));
    }

    /**
     * Delete hotel (and all its rooms)
     * 
     * Endpoint: DELETE /api/hotels/{id}
     * Access: Protected (only hotel owner can delete)
     * 
     * @param id Hotel ID
     * @param authentication JWT authentication token
     * @return HTTP 204 No Content on success
     * @throws ForbiddenException if user is not the owner
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHotel(
            @PathVariable Long id,
            Authentication authentication) {
        String ownerId = authentication.getName();
        log.info("Delete hotel request: id={}, owner={}", id, ownerId);
        hotelService.deleteHotel(id, ownerId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Check hotel availability for booking
     * 
     * Endpoint: GET /api/hotels/{id}/availability?startDate=2026-03-01&endDate=2026-03-05
     * Access: Public
     * 
     * Used by booking-service to verify hotel can accept bookings
     * 
     * @param id Hotel ID
     * @param startDate Booking start date (YYYY-MM-DD)
     * @param endDate Booking end date (YYYY-MM-DD)
     * @return Availability status and available room count
     */
    @GetMapping("/{id}/availability")
    public ApiResponse<AvailabilityResponse> checkAvailability(
            @PathVariable Long id,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        log.debug("Check availability for hotel: {}, dates: {} to {}", id, startDate, endDate);
        return ApiResponse.success(hotelService.checkAvailability(id, startDate, endDate));
    }
}
