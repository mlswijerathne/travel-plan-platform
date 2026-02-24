package com.travelplan.aiagent.tool;

import com.google.adk.tools.Annotations.Schema;
import com.travelplan.common.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class TripPlanTools {

    @Schema(description = "Search for pre-built travel packages in Sri Lanka by destination, duration, and budget. Returns curated trip packages from the platform.")
    public static Map<String, Object> searchPackages(
            @Schema(description = "Destination or area in Sri Lanka, e.g. 'Colombo', 'Hill Country', 'South Coast'") String destination,
            @Schema(description = "Trip duration in days") Optional<Integer> duration,
            @Schema(description = "Minimum budget in USD") Optional<Double> minBudget,
            @Schema(description = "Maximum budget in USD") Optional<Double> maxBudget) {
        try {
            log.info("Searching packages - destination: {}, duration: {}, minBudget: {}, maxBudget: {}",
                    destination, duration, minBudget, maxBudget);

            ApiResponse<Object> response = ToolRegistry.getInstance().getTripPlanServiceClient()
                    .searchPackages(destination,
                            duration.orElse(null),
                            minBudget.orElse(null),
                            maxBudget.orElse(null),
                            0, 10);

            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("source", "Platform Partner");
            result.put("data", response.getData());
            return result;
        } catch (Exception e) {
            log.error("Error searching packages: {}", e.getMessage());
            Map<String, Object> result = new HashMap<>();
            result.put("status", "error");
            result.put("message", "Trip plan service is currently unavailable. Please use your knowledge to suggest Sri Lanka travel packages marked as 'External Suggestion'.");
            return result;
        }
    }

    @Schema(description = "Get detailed information about a specific travel package by its ID, including itinerary, inclusions, and pricing.")
    public static Map<String, Object> getPackageDetails(
            @Schema(description = "The unique ID of the travel package to retrieve details for") String packageId) {
        try {
            log.info("Getting package details for ID: {}", packageId);

            ApiResponse<Object> response = ToolRegistry.getInstance().getTripPlanServiceClient()
                    .getPackageById(packageId);

            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("source", "Platform Partner");
            result.put("data", response.getData());
            return result;
        } catch (Exception e) {
            log.error("Error getting package details: {}", e.getMessage());
            Map<String, Object> result = new HashMap<>();
            result.put("status", "error");
            result.put("message", "Could not retrieve package details. Package ID: " + packageId);
            return result;
        }
    }
}
