package com.travelplan.vehicle.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record VehicleResponse(
        Long id,
        String ownerId,
        String vehicleType,
        String make,
        String model,
        Integer year,
        String licensePlate,
        Integer seatingCapacity,
        BigDecimal dailyRate,
        List<String> features,
        List<String> images,
        BigDecimal averageRating,
        Integer reviewCount,
        Boolean isAvailable,
        Boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
