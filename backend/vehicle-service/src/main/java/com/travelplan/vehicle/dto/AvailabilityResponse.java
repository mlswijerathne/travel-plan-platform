package com.travelplan.vehicle.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record AvailabilityResponse(
        Long vehicleId,
        Boolean available,
        BigDecimal dailyRate,
        List<LocalDate> bookedDates) {
}
