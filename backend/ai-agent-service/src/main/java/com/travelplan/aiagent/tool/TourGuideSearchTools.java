package com.travelplan.aiagent.tool;

import com.google.adk.tools.Annotations.Schema;
import com.travelplan.common.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class TourGuideSearchTools {

    @Schema(description = "Search for tour guides in Sri Lanka by location, language, and specialization. Returns available tour guides from the platform.")
    public static Map<String, Object> searchTourGuides(
            @Schema(description = "Location/area for the tour guide, e.g. 'Colombo', 'Sigiriya', 'Ella'") String location,
            @Schema(description = "Preferred language of the tour guide, e.g. 'English', 'French', 'German'") Optional<String> language,
            @Schema(description = "Specialization area, e.g. 'Historical', 'Wildlife', 'Adventure', 'Cultural'") Optional<String> specialization) {
        try {
            log.info("Searching tour guides - location: {}, language: {}, specialization: {}",
                    location, language, specialization);

            ApiResponse<Object> response = ToolRegistry.getInstance().getTourGuideServiceClient()
                    .searchTourGuides(location,
                            language.orElse(null),
                            specialization.orElse(null),
                            0, 10);

            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("source", "Platform Partner");
            result.put("data", response.getData());
            return result;
        } catch (Exception e) {
            log.error("Error searching tour guides: {}", e.getMessage());
            Map<String, Object> result = new HashMap<>();
            result.put("status", "error");
            result.put("message", "Tour guide service is currently unavailable. Please use your knowledge to suggest tour guide options marked as 'External Suggestion'.");
            return result;
        }
    }

    @Schema(description = "Get detailed information about a specific tour guide by their ID, including qualifications, languages, and reviews.")
    public static Map<String, Object> getGuideDetails(
            @Schema(description = "The unique ID of the tour guide to retrieve details for") String guideId) {
        try {
            log.info("Getting tour guide details for ID: {}", guideId);

            ApiResponse<Object> response = ToolRegistry.getInstance().getTourGuideServiceClient()
                    .getTourGuideById(guideId);

            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("source", "Platform Partner");
            result.put("data", response.getData());
            return result;
        } catch (Exception e) {
            log.error("Error getting tour guide details: {}", e.getMessage());
            Map<String, Object> result = new HashMap<>();
            result.put("status", "error");
            result.put("message", "Could not retrieve tour guide details. Guide ID: " + guideId);
            return result;
        }
    }
}
