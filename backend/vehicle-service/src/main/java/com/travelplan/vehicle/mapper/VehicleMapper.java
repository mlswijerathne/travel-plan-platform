package com.travelplan.vehicle.mapper;

import com.travelplan.vehicle.dto.VehicleResponse;
import com.travelplan.vehicle.entity.Vehicle;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class VehicleMapper {

    public VehicleResponse toDTO(Vehicle vehicle, boolean isAvailableOverride) {
        if (vehicle == null) {
            return null;
        }

        return new VehicleResponse(
                vehicle.getId(),
                vehicle.getOwnerId(),
                vehicle.getVehicleType(),
                vehicle.getMake(),
                vehicle.getModel(),
                vehicle.getYear(),
                vehicle.getLicensePlate(),
                vehicle.getSeatingCapacity(),
                vehicle.getDailyRate(),
                vehicle.getFeatures(),
                vehicle.getImages(),
                vehicle.getAverageRating(),
                vehicle.getReviewCount(),
                // Use the override value for availability
                isAvailableOverride,
                vehicle.getIsActive(),
                vehicle.getCreatedAt(),
                vehicle.getUpdatedAt());
    }

    public List<VehicleResponse> toDTOList(List<Vehicle> vehicles) {
        return vehicles.stream()
                .map(v -> toDTO(v, v.getIsAvailable())) // Default to entity's availability if not overridden
                .collect(Collectors.toList());
    }
}
