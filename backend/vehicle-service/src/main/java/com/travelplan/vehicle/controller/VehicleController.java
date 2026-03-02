package com.travelplan.vehicle.controller;

import com.travelplan.vehicle.dto.AvailabilityResponse;
import com.travelplan.vehicle.dto.VehicleRequest;
import com.travelplan.vehicle.dto.VehicleResponse;
import com.travelplan.vehicle.service.VehicleService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    @Autowired
    private VehicleService vehicleService;

    @GetMapping
    public ResponseEntity<?> searchVehicles(
            @RequestParam(required = false) String vehicleType,
            @RequestParam(required = false) Integer minCapacity,
            @RequestParam(required = false) BigDecimal minDailyRate,
            @RequestParam(required = false) BigDecimal maxDailyRate,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            return ResponseEntity.ok(
                    vehicleService.searchVehicles(vehicleType, minCapacity, minDailyRate, maxDailyRate, query, page,
                            size));
        } catch (Exception e) {
            // Expose real error for debugging
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of(
                            "error", e.getClass().getName(),
                            "message", e.getMessage() != null ? e.getMessage() : "null",
                            "cause", e.getCause() != null ? e.getCause().getMessage() : "null"));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehicleResponse> getVehicleById(@PathVariable Long id) {
        return vehicleService.getVehicleById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<VehicleResponse> createVehicle(
            @Valid @RequestBody VehicleRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        // In a real scenario,userId would come from JWT security context
        String ownerId = userId != null ? userId : "admin";
        return ResponseEntity.status(HttpStatus.CREATED).body(vehicleService.createVehicle(request, ownerId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VehicleResponse> updateVehicle(
            @PathVariable Long id,
            @Valid @RequestBody VehicleRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        String ownerId = userId != null ? userId : "admin";
        return vehicleService.updateVehicle(id, request, ownerId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVehicle(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        String ownerId = userId != null ? userId : "admin";
        if (vehicleService.deleteVehicle(id, ownerId)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/availability")
    public ResponseEntity<AvailabilityResponse> checkAvailability(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(vehicleService.checkAvailability(id, startDate, endDate));
    }

    @GetMapping("/owner")
    public ResponseEntity<List<VehicleResponse>> getOwnerVehicles(
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        String ownerId = userId != null ? userId : "admin";
        return ResponseEntity.ok(vehicleService.getVehiclesByOwner(ownerId));
    }
}