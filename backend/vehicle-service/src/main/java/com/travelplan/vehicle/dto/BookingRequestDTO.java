package com.travelplan.vehicle.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Data Transfer Object for Booking Requests.
 * Ensure field names match the frontend JSON exactly.
 */
public record BookingRequestDTO(
                Long vehicleId,
                String customerName,
                String customerEmail,
                LocalDate startDate,
                LocalDate endDate,
                BigDecimal totalPrice // Added to match frontend 'totalPrice' key
) {
}