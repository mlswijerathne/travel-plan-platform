package com.travelplan.vehicle.dto;

import java.math.BigDecimal;
import java.util.List;

public record VehicleDTO(
        Long id,
        String ownerId,
        String vehicleType,
        String make,
        String model,
        Integer year,
        String licensePlate,
        Integer seatingCapacity,
        BigDecimal dailyRate,
        String imageUrl,
        List<String> features,
        BigDecimal averageRating,
        Integer reviewCount,
        Boolean isAvailable,
        Boolean isActive) {
}
