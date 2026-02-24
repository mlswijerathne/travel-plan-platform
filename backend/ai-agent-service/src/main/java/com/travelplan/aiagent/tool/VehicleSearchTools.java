package com.travelplan.aiagent.tool;

import com.google.adk.tools.Annotations.Schema;
import com.travelplan.common.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class VehicleSearchTools {

    @Schema(description = "Search for rental vehicles in Sri Lanka by type, location, and price range. Returns available vehicles from the platform.")
    public static Map<String, Object> searchVehicles(
            @Schema(description = "Type of vehicle, e.g. 'Car', 'Van', 'SUV', 'TukTuk', 'Bus'") Optional<String> type,
            @Schema(description = "Pickup location, e.g. 'Colombo', 'Kandy', 'Airport'") String location,
            @Schema(description = "Minimum daily rental price in USD") Optional<Double> minPrice,
            @Schema(description = "Maximum daily rental price in USD") Optional<Double> maxPrice) {
        try {
            log.info("Searching vehicles - type: {}, location: {}, minPrice: {}, maxPrice: {}",
                    type, location, minPrice, maxPrice);

            ApiResponse<Object> response = ToolRegistry.getInstance().getVehicleServiceClient()
                    .searchVehicles(type.orElse(null), location,
                            minPrice.orElse(null),
                            maxPrice.orElse(null),
                            0, 10);

            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("source", "Platform Partner");
            result.put("data", response.getData());
            return result;
        } catch (Exception e) {
            log.error("Error searching vehicles: {}", e.getMessage());
            Map<String, Object> result = new HashMap<>();
            result.put("status", "error");
            result.put("message", "Vehicle service is currently unavailable. Please use your knowledge to suggest vehicle options marked as 'External Suggestion'.");
            return result;
        }
    }

    @Schema(description = "Get detailed information about a specific vehicle by its ID, including availability, features, and pricing.")
    public static Map<String, Object> getVehicleDetails(
            @Schema(description = "The unique ID of the vehicle to retrieve details for") String vehicleId) {
        try {
            log.info("Getting vehicle details for ID: {}", vehicleId);

            ApiResponse<Object> response = ToolRegistry.getInstance().getVehicleServiceClient()
                    .getVehicleById(vehicleId);

            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("source", "Platform Partner");
            result.put("data", response.getData());
            return result;
        } catch (Exception e) {
            log.error("Error getting vehicle details: {}", e.getMessage());
            Map<String, Object> result = new HashMap<>();
            result.put("status", "error");
            result.put("message", "Could not retrieve vehicle details. Vehicle ID: " + vehicleId);
            return result;
        }
    }
}
