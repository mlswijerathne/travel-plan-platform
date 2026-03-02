package com.travelplan.hotel.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomResponse {

    private Long id;
    private Long hotelId;
    private String roomType;
    private String name;
    private String description;
    private BigDecimal pricePerNight;
    private Integer maxOccupancy;
    private List<String> amenities;
    private Integer totalRooms;
    private Boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;
}
