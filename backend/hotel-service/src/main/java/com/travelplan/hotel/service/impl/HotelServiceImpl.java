package com.travelplan.hotel.service.impl;

import com.travelplan.common.dto.PaginatedResponse;
import com.travelplan.common.exception.ForbiddenException;
import com.travelplan.common.exception.ResourceNotFoundException;
import com.travelplan.hotel.dto.*;
import com.travelplan.hotel.dto.request.CreateHotelRequest;
import com.travelplan.hotel.dto.request.UpdateHotelRequest;
import com.travelplan.hotel.dto.response.AvailabilityResponse;
import com.travelplan.hotel.dto.response.HotelResponse;
import com.travelplan.hotel.dto.response.RoomResponse;
import com.travelplan.hotel.entity.Hotel;
import com.travelplan.hotel.entity.Room;
import com.travelplan.hotel.mapper.HotelMapper;
import com.travelplan.hotel.mapper.RoomMapper;
import com.travelplan.hotel.repository.HotelRepository;
import com.travelplan.hotel.repository.RoomRepository;
import com.travelplan.hotel.service.HotelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HotelServiceImpl implements HotelService {

    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final HotelMapper hotelMapper;
    private final RoomMapper roomMapper;

    @Override
    @Transactional
    public HotelResponse createHotel(String ownerId, CreateHotelRequest request) {
        log.info("Creating hotel for owner: {}", ownerId);
        Hotel hotel = hotelMapper.toEntity(request, ownerId);
        Hotel savedHotel = hotelRepository.save(hotel);
        log.info("Hotel created with id: {}", savedHotel.getId());
        return hotelMapper.toResponse(savedHotel);
    }

    @Override
    @Transactional(readOnly = true)
    public HotelResponse getHotelById(Long id) {
        log.debug("Fetching hotel by id: {}", id);
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel", "id", id));
        return hotelMapper.toResponse(hotel);
    }

    @Override
    @Transactional(readOnly = true)
    public HotelResponse getHotelByIdWithRooms(Long id) {
        log.debug("Fetching hotel with rooms by id: {}", id);
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel", "id", id));
        List<Room> rooms = roomRepository.findByHotelId(id);
        List<RoomResponse> roomResponses = roomMapper.toResponseList(rooms);
        return hotelMapper.toResponseWithRooms(hotel, roomResponses);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<HotelResponse> getAllHotels(int page, int size) {
        log.debug("Fetching all hotels - page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Hotel> hotelPage = hotelRepository.findByIsActiveTrue(pageable);
        List<HotelResponse> hotels = hotelPage.getContent().stream()
                .map(hotelMapper::toResponse)
                .toList();
        return PaginatedResponse.of(hotels, page, size, hotelPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<HotelResponse> getHotelsByOwner(String ownerId, int page, int size) {
        log.debug("Fetching hotels for owner: {}", ownerId);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Hotel> hotelPage = hotelRepository.findByOwnerId(ownerId, pageable);
        List<HotelResponse> hotels = hotelPage.getContent().stream()
                .map(hotelMapper::toResponse)
                .toList();
        return PaginatedResponse.of(hotels, page, size, hotelPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<HotelResponse> searchHotels(String city, Integer starRating, int page, int size) {
        log.debug("Searching hotels - city: {}, starRating: {}", city, starRating);
        Pageable pageable = PageRequest.of(page, size, Sort.by("averageRating").descending());
        Page<Hotel> hotelPage = hotelRepository.searchHotels(city, starRating, pageable);
        List<HotelResponse> hotels = hotelPage.getContent().stream()
                .map(hotelMapper::toResponse)
                .toList();
        return PaginatedResponse.of(hotels, page, size, hotelPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<HotelResponse> searchHotelsByQuery(String query, int page, int size) {
        log.debug("Searching hotels by query: {}", query);
        Pageable pageable = PageRequest.of(page, size);
        Page<Hotel> hotelPage = hotelRepository.searchByQuery(query, pageable);
        List<HotelResponse> hotels = hotelPage.getContent().stream()
                .map(hotelMapper::toResponse)
                .toList();
        return PaginatedResponse.of(hotels, page, size, hotelPage.getTotalElements());
    }

    @Override
    @Transactional
    public HotelResponse updateHotel(Long id, String ownerId, UpdateHotelRequest request) {
        log.info("Updating hotel: {}", id);
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel", "id", id));

        if (!hotel.getOwnerId().equals(ownerId)) {
            throw new ForbiddenException("You don't have permission to update this hotel");
        }

        hotelMapper.updateEntityFromRequest(hotel, request);
        Hotel updatedHotel = hotelRepository.save(hotel);
        log.info("Hotel updated: {}", id);
        return hotelMapper.toResponse(updatedHotel);
    }

    @Override
    @Transactional
    public void deleteHotel(Long id, String ownerId) {
        log.info("Deleting hotel: {}", id);
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel", "id", id));

        if (!hotel.getOwnerId().equals(ownerId)) {
            throw new ForbiddenException("You don't have permission to delete this hotel");
        }

        hotelRepository.delete(hotel);
        log.info("Hotel deleted: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public AvailabilityResponse checkAvailability(Long hotelId, String startDate, String endDate) {
        log.debug("Checking availability for hotel: {}", hotelId);
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel", "id", hotelId));

        if (!hotel.getIsActive()) {
            return AvailabilityResponse.builder()
                    .hotelId(hotelId)
                    .hotelName(hotel.getName())
                    .available(false)
                    .availableRooms(0)
                    .message("Hotel is currently inactive")
                    .build();
        }

        List<Room> activeRooms = roomRepository.findActiveRoomsByHotelId(hotelId);
        int totalAvailableRooms = activeRooms.stream()
                .mapToInt(Room::getTotalRooms)
                .sum();

        boolean available = totalAvailableRooms > 0;
        String message = available ?
                "Rooms available for booking" :
                "No rooms available for the selected dates";

        return AvailabilityResponse.builder()
                .hotelId(hotelId)
                .hotelName(hotel.getName())
                .available(available)
                .availableRooms(totalAvailableRooms)
                .message(message)
                .build();
    }

    @Override
    @Transactional
    public void updateHotelRating(Long hotelId, BigDecimal newRating, int reviewCount) {
        log.info("Updating rating for hotel: {}, newRating: {}, reviewCount: {}", 
                hotelId, newRating, reviewCount);
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel", "id", hotelId));
        
        hotel.setAverageRating(newRating);
        hotel.setReviewCount(reviewCount);
        hotelRepository.save(hotel);
        log.info("Hotel rating updated for hotel: {}", hotelId);
    }
}
