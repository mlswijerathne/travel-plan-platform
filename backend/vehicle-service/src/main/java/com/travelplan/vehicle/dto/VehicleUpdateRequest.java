package com.travelplan.vehicle.dto;

import java.math.BigDecimal;
import java.util.List;

public record VehicleUpdateRequest(
        String vehicleType,
        String make,
        String model,
        Integer year,
        String licensePlate,
        Integer seatingCapacity,
        BigDecimal dailyRate,
        List<String> features,
        List<String> images,
        Boolean isAvailable) {
}
