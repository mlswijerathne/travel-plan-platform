package com.travelplan.vehicle.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

public record VehicleRequest(
        @NotBlank(message = "Vehicle type is required") @Pattern(regexp = "^(CAR|SUV|VAN|BUS|TUK_TUK|MOTORBIKE)$", message = "Vehicle type must be CAR, SUV, VAN, BUS, TUK_TUK, or MOTORBIKE") String vehicleType,

        @NotBlank(message = "Make is required") @Size(max = 100) String make,

        @NotBlank(message = "Model is required") @Size(max = 100) String model,

        Integer year,

        @NotBlank(message = "License plate is required") @Size(max = 20) String licensePlate,

        @NotNull(message = "Seating capacity is required") @Min(1) Integer seatingCapacity,

        @NotNull(message = "Daily rate is required") @DecimalMin("0.01") BigDecimal dailyRate,

        List<String> features,

        List<String> images,

        Boolean isAvailable) {
}