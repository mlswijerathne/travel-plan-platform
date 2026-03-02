package com.travelplan.vehicle.service;

import com.travelplan.vehicle.dto.AvailabilityResponse;
import com.travelplan.vehicle.dto.VehicleRequest;
import com.travelplan.vehicle.dto.VehicleResponse;
import com.travelplan.vehicle.entity.Vehicle;
import com.travelplan.vehicle.mapper.VehicleMapper;
import com.travelplan.vehicle.repository.BookingRepository;
import com.travelplan.vehicle.repository.VehicleRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class VehicleService {

    private static final Logger log = LoggerFactory.getLogger(VehicleService.class);

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private VehicleMapper vehicleMapper;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(uploadDir));
        } catch (IOException e) {
            log.warn("Could not create upload folder: {}", e.getMessage());
        }
    }

    private boolean isVehicleCurrentlyAvailable(Vehicle vehicle) {
        if (vehicle == null || !vehicle.getIsActive() || !vehicle.getIsAvailable()) {
            return false;
        }
        LocalDate today = LocalDate.now();
        LocalDate distantFuture = today.plusYears(10);

        return !bookingRepository.existsOverlappingBooking(vehicle.getId(), "CONFIRMED", today, distantFuture);
    }

    public Page<VehicleResponse> searchVehicles(
            String vehicleType,
            Integer minCapacity,
            BigDecimal minDailyRate,
            BigDecimal maxDailyRate,
            String query,
            int page,
            int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Vehicle> vehicles = vehicleRepository.searchVehicles(vehicleType, minCapacity, minDailyRate, maxDailyRate,
                query, pageable);
        return vehicles.map(v -> vehicleMapper.toDTO(v, isVehicleCurrentlyAvailable(v)));
    }

    public Optional<VehicleResponse> getVehicleById(Long id) {
        return vehicleRepository.findById(id)
                .map(v -> vehicleMapper.toDTO(v, isVehicleCurrentlyAvailable(v)));
    }

    public List<VehicleResponse> getVehiclesByOwner(String ownerId) {
        List<Vehicle> vehicles = vehicleRepository.findByOwnerIdAndIsActiveTrue(ownerId);
        return vehicles.stream()
                .map(v -> vehicleMapper.toDTO(v, isVehicleCurrentlyAvailable(v)))
                .collect(Collectors.toList());
    }

    @Transactional
    public VehicleResponse createVehicle(VehicleRequest request, String ownerId) {
        // NEW: Enforce unique license plate requirement
        if (vehicleRepository.existsByLicensePlate(request.licensePlate())) {
            throw new IllegalStateException("Vehicle already exists with licensePlate: '" + request.licensePlate() + "'");
        }

        Vehicle vehicle = new Vehicle();
        updateVehicleFromRequest(vehicle, request);
        vehicle.setOwnerId(ownerId != null ? ownerId : "anonymous");
        vehicle.setIsActive(true);
        vehicle.setIsAvailable(true);
        vehicle.setAverageRating(BigDecimal.ZERO);
        vehicle.setReviewCount(0);

        Vehicle saved = vehicleRepository.save(vehicle);
        return vehicleMapper.toDTO(saved, true);
    }

    @Transactional
    public Optional<VehicleResponse> updateVehicle(Long id, VehicleRequest request, String ownerId) {
        return vehicleRepository.findById(id)
                .filter(v -> v.getOwnerId().equals(ownerId) || "admin".equals(ownerId))
                .map(existing -> {
                    // NEW: Ensure they aren't changing the plate to one that belongs to another car
                    if (!existing.getLicensePlate().equals(request.licensePlate()) && 
                        vehicleRepository.existsByLicensePlate(request.licensePlate())) {
                        throw new IllegalStateException("Vehicle already exists with licensePlate: '" + request.licensePlate() + "'");
                    }

                    updateVehicleFromRequest(existing, request);
                    Vehicle saved = vehicleRepository.save(existing);
                    return vehicleMapper.toDTO(saved, isVehicleCurrentlyAvailable(saved));
                });
    }

    @Transactional
    public boolean deleteVehicle(Long id, String ownerId) {
        return vehicleRepository.findById(id)
                .filter(v -> v.getOwnerId().equals(ownerId) || "admin".equals(ownerId))
                .map(v -> {
                    v.setIsActive(false);
                    vehicleRepository.save(v);
                    return true;
                }).orElse(false);
    }

    public AvailabilityResponse checkAvailability(Long vehicleId, LocalDate startDate, LocalDate endDate) {
        boolean isBooked = bookingRepository.existsOverlappingBooking(vehicleId, "CONFIRMED", startDate, endDate);
        Vehicle vehicle = vehicleRepository.findById(vehicleId).orElse(null);

        boolean available = vehicle != null && vehicle.getIsActive() && vehicle.getIsAvailable() && !isBooked;
        BigDecimal rate = vehicle != null ? vehicle.getDailyRate() : BigDecimal.ZERO;

        return new AvailabilityResponse(vehicleId, available, rate, List.of());
    }

    private void updateVehicleFromRequest(Vehicle vehicle, VehicleRequest request) {
        vehicle.setVehicleType(request.vehicleType());
        vehicle.setMake(request.make());
        vehicle.setModel(request.model());
        vehicle.setYear(request.year());
        vehicle.setLicensePlate(request.licensePlate());
        vehicle.setSeatingCapacity(request.seatingCapacity());
        vehicle.setDailyRate(request.dailyRate());
        vehicle.setFeatures(request.features());
        vehicle.setImages(request.images());
        
        if (request.isAvailable() != null) {
            vehicle.setIsAvailable(request.isAvailable());
        }
    }
}