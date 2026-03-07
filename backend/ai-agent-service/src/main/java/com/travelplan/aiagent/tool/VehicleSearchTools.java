package com.travelplan.aiagent.tool;

import com.google.adk.tools.Annotations.Schema;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class VehicleSearchTools {

    @Schema(description = "Search for rental vehicles in Sri Lanka by type and price range. Use the query param to search by text. Returns available vehicles from the platform.")
    public static Map<String, Object> searchVehicles(
            @Schema(description = "Type of vehicle, e.g. 'Car', 'Van', 'SUV', 'TukTuk', 'Bus'. Pass empty string to search all.") String vehicleType,
            @Schema(description = "Minimum daily rental price in USD, pass 0 to skip") double minDailyRate,
            @Schema(description = "Maximum daily rental price in USD, pass 0 to skip") double maxDailyRate,
            @Schema(description = "Text search query for vehicle make, model, or features. Pass empty string to skip.") String query) {
        try {
            log.info("Searching vehicles - vehicleType: {}, minDailyRate: {}, maxDailyRate: {}, query: {}",
                    vehicleType, minDailyRate, maxDailyRate, query);

            String typeParam = (vehicleType != null && !vehicleType.isBlank()) ? vehicleType : null;
            BigDecimal minParam = minDailyRate > 0 ? BigDecimal.valueOf(minDailyRate) : null;
            BigDecimal maxParam = maxDailyRate > 0 ? BigDecimal.valueOf(maxDailyRate) : null;
            String queryParam = (query != null && !query.isBlank()) ? query : null;

            Object response = ToolRegistry.getInstance().getVehicleServiceClient()
                    .searchVehicles(typeParam, minParam, maxParam, queryParam, 0, 10);

            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("source", "Platform Partner");
            result.put("data", response);
            return result;
        } catch (Exception e) {
            log.error("Error searching vehicles: {} - {}", e.getClass().getSimpleName(), e.getMessage(), e);
            Map<String, Object> result = new HashMap<>();
            result.put("status", "error");
            result.put("message", "Vehicle service is currently unavailable. Error: " + e.getMessage());
            return result;
        }
    }

    @Schema(description = "Get detailed information about a specific vehicle by its ID, including availability, features, and pricing.")
    public static Map<String, Object> getVehicleDetails(
            @Schema(description = "The unique ID of the vehicle to retrieve details for") String vehicleId) {
        try {
            log.info("Getting vehicle details for ID: {}", vehicleId);

            Object response = ToolRegistry.getInstance().getVehicleServiceClient()
                    .getVehicleById(vehicleId);

            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("source", "Platform Partner");
            result.put("data", response);
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
