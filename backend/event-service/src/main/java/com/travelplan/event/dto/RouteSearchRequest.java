package com.travelplan.event.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * TASK-7.1: Request parameters for route-based event discovery.
 *
 * Defines a travel route from (startLat, startLng) to (endLat, endLng)
 * and finds PUBLISHED events within a corridor of radiusKm on either side.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteSearchRequest {

    @NotNull(message = "Start latitude is required")
    @DecimalMin(value = "-90.0", message = "Latitude must be >= -90")
    @DecimalMax(value = "90.0",  message = "Latitude must be <= 90")
    private Double startLat;

    @NotNull(message = "Start longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude must be >= -180")
    @DecimalMax(value = "180.0",  message = "Longitude must be <= 180")
    private Double startLng;

    @NotNull(message = "End latitude is required")
    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    private Double endLat;

    @NotNull(message = "End longitude is required")
    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    private Double endLng;

    /** Half-width of the corridor in km. Default 50 km. */
    @Positive(message = "Radius must be positive")
    @Builder.Default
    private Double radiusKm = 50.0;

    /** Optional: only return events on or after this date. */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateFrom;

    /** Optional: only return events on or before this date. */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateTo;
}
