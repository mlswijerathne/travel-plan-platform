package com.travelplan.hotel.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRoomRequest {

    @Size(max = 50, message = "Room type cannot exceed 50 characters")
    private String roomType;

    @Size(max = 100, message = "Room name cannot exceed 100 characters")
    private String name;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal pricePerNight;

    @Min(value = 1, message = "Max occupancy must be at least 1")
    private Integer maxOccupancy;

    private List<String> amenities;

    @Min(value = 1, message = "Total rooms must be at least 1")
    private Integer totalRooms;

    private Boolean isActive;
}
