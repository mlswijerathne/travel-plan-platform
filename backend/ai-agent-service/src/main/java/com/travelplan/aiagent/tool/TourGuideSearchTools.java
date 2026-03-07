package com.travelplan.aiagent.tool;

import com.google.adk.tools.Annotations.Schema;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class TourGuideSearchTools {

    @Schema(description = "Search for tour guides in Sri Lanka by language, specialization, or text query. Use the query param to search by location or name. Returns available tour guides from the platform.")
    public static Map<String, Object> searchTourGuides(
            @Schema(description = "Preferred language of the tour guide, e.g. 'English', 'French', 'German'. Pass empty string to skip.") String language,
            @Schema(description = "Specialization area, e.g. 'Historical', 'Wildlife', 'Adventure', 'Cultural'. Pass empty string to skip.") String specialization,
            @Schema(description = "Text search query for name, location, or bio, e.g. 'Colombo', 'Sigiriya', 'Ella'. Pass empty string to skip.") String query) {
        try {
            log.info("Searching tour guides - language: {}, specialization: {}, query: {}",
                    language, specialization, query);

            String langParam = (language != null && !language.isBlank()) ? language : null;
            String specParam = (specialization != null && !specialization.isBlank()) ? specialization : null;
            String queryParam = (query != null && !query.isBlank()) ? query : null;

            Object response = ToolRegistry.getInstance().getTourGuideServiceClient()
                    .searchTourGuides(langParam, specParam, queryParam, 0, 10);

            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("source", "Platform Partner");
            result.put("data", response);
            return result;
        } catch (Exception e) {
            log.error("Error searching tour guides: {} - {}", e.getClass().getSimpleName(), e.getMessage(), e);
            Map<String, Object> result = new HashMap<>();
            result.put("status", "error");
            result.put("message", "Tour guide service is currently unavailable. Error: " + e.getMessage());
            return result;
        }
    }

    @Schema(description = "Get detailed information about a specific tour guide by their ID, including qualifications, languages, and reviews.")
    public static Map<String, Object> getGuideDetails(
            @Schema(description = "The unique ID of the tour guide to retrieve details for") String guideId) {
        try {
            log.info("Getting tour guide details for ID: {}", guideId);

            var response = ToolRegistry.getInstance().getTourGuideServiceClient()
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
