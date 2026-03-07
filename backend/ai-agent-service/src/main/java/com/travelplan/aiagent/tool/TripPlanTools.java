package com.travelplan.aiagent.tool;

import com.google.adk.tools.Annotations.Schema;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class TripPlanTools {

    @Schema(description = "Search for pre-built travel packages in Sri Lanka by destination, duration, and budget. Returns curated trip packages from the platform.")
    public static Map<String, Object> searchPackages(
            @Schema(description = "Destination or area in Sri Lanka, e.g. 'Colombo', 'Hill Country', 'South Coast'. Pass empty string to search all.") String destination,
            @Schema(description = "Maximum trip duration in days, pass 0 to skip filtering") int durationDays,
            @Schema(description = "Minimum budget in USD, pass 0 to skip") double minBudget,
            @Schema(description = "Maximum budget in USD, pass 0 to skip") double maxBudget) {
        try {
            log.info("Searching packages - destination: {}, durationDays: {}, minBudget: {}, maxBudget: {}",
                    destination, durationDays, minBudget, maxBudget);

            String destParam = (destination != null && !destination.isBlank()) ? destination : null;
            Integer durationParam = durationDays > 0 ? durationDays : null;
            Double minParam = minBudget > 0 ? minBudget : null;
            Double maxParam = maxBudget > 0 ? maxBudget : null;

            Object response = ToolRegistry.getInstance().getTripPlanServiceClient()
                    .searchPackages(destParam, durationParam, minParam, maxParam, null, 0, 10);

            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("source", "Platform Partner");
            result.put("data", response);
            return result;
        } catch (Exception e) {
            log.error("Error searching packages: {} - {}", e.getClass().getSimpleName(), e.getMessage(), e);
            Map<String, Object> result = new HashMap<>();
            result.put("status", "error");
            result.put("message", "Trip plan service is currently unavailable. Error: " + e.getMessage());
            return result;
        }
    }

    @Schema(description = "Get detailed information about a specific travel package by its ID, including itinerary, inclusions, and pricing.")
    public static Map<String, Object> getPackageDetails(
            @Schema(description = "The unique ID of the travel package to retrieve details for") String packageId) {
        try {
            log.info("Getting package details for ID: {}", packageId);

            Object response = ToolRegistry.getInstance().getTripPlanServiceClient()
                    .getPackageById(packageId);

            // Trip-plan-service returns {"data": packageResponse} — extract the inner data if present
            Object data = response;
            if (response instanceof Map<?, ?> responseMap && responseMap.containsKey("data")) {
                data = responseMap.get("data");
            }

            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("source", "Platform Partner");
            result.put("data", data);
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
