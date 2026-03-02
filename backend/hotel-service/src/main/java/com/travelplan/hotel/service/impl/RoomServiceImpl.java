package com.travelplan.hotel.service.impl;

import com.travelplan.common.dto.PaginatedResponse;
import com.travelplan.common.exception.ForbiddenException;
import com.travelplan.common.exception.ResourceNotFoundException;
import com.travelplan.hotel.dto.request.CreateRoomRequest;
import com.travelplan.hotel.dto.request.UpdateRoomRequest;
import com.travelplan.hotel.dto.response.RoomResponse;
import com.travelplan.hotel.entity.Hotel;
import com.travelplan.hotel.entity.Room;
import com.travelplan.hotel.mapper.RoomMapper;
import com.travelplan.hotel.repository.HotelRepository;
import com.travelplan.hotel.repository.RoomRepository;
import com.travelplan.hotel.service.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final RoomMapper roomMapper;

    @Override
    @Transactional
    public RoomResponse createRoom(String ownerId, CreateRoomRequest request) {
        log.info("Creating room for hotel: {}", request.getHotelId());
        Hotel hotel = hotelRepository.findById(request.getHotelId())
                .orElseThrow(() -> new ResourceNotFoundException("Hotel", "id", request.getHotelId()));

        if (!hotel.getOwnerId().equals(ownerId)) {
            throw new ForbiddenException("You don't have permission to add rooms to this hotel");
        }

        Room room = roomMapper.toEntity(request, hotel);
        Room savedRoom = roomRepository.save(room);
        log.info("Room created with id: {}", savedRoom.getId());
        return roomMapper.toResponse(savedRoom);
    }

    @Override
    @Transactional(readOnly = true)
    public RoomResponse getRoomById(Long id) {
        log.debug("Fetching room by id: {}", id);
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room", "id", id));
        return roomMapper.toResponse(room);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomResponse> getRoomsByHotelId(Long hotelId) {
        log.debug("Fetching rooms for hotel: {}", hotelId);
        List<Room> rooms = roomRepository.findByHotelId(hotelId);
        return roomMapper.toResponseList(rooms);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<RoomResponse> getRoomsByHotelIdPaginated(Long hotelId, int page, int size) {
        log.debug("Fetching rooms for hotel: {} - page: {}, size: {}", hotelId, page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Room> roomPage = roomRepository.findByHotelId(hotelId, pageable);
        List<RoomResponse> rooms = roomPage.getContent().stream()
                .map(roomMapper::toResponse)
                .toList();
        return PaginatedResponse.of(rooms, page, size, roomPage.getTotalElements());
    }

    @Override
    @Transactional
    public RoomResponse updateRoom(Long id, String ownerId, UpdateRoomRequest request) {
        log.info("Updating room: {}", id);
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room", "id", id));

        Hotel hotel = room.getHotel();
        if (!hotel.getOwnerId().equals(ownerId)) {
            throw new ForbiddenException("You don't have permission to update this room");
        }

        roomMapper.updateEntityFromRequest(room, request);
        Room updatedRoom = roomRepository.save(room);
        log.info("Room updated: {}", id);
        return roomMapper.toResponse(updatedRoom);
    }

    @Override
    @Transactional
    public void deleteRoom(Long id, String ownerId) {
        log.info("Deleting room: {}", id);
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room", "id", id));

        Hotel hotel = room.getHotel();
        if (!hotel.getOwnerId().equals(ownerId)) {
            throw new ForbiddenException("You don't have permission to delete this room");
        }

        roomRepository.delete(room);
        log.info("Room deleted: {}", id);
    }
}
