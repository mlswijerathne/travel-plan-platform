package com.travelplan.aiagent.tool;

import com.google.adk.tools.Annotations.Schema;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class HotelSearchTools {

    @Schema(description = "Search for hotels in Sri Lanka by city and star rating. Returns a list of available hotels from the platform.")
    public static Map<String, Object> searchHotels(
            @Schema(description = "City name to search hotels in, e.g. 'Colombo', 'Kandy', 'Galle'") String city,
            @Schema(description = "Minimum star rating (1-5), pass 0 or omit to skip filtering") int starRating) {
        try {
            log.info("Searching hotels - city: {}, starRating: {}", city, starRating);

            Integer starRatingParam = starRating > 0 ? starRating : null;
            Object response = ToolRegistry.getInstance().getHotelServiceClient()
                    .searchHotels(city, starRatingParam, 0, 10);

            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("source", "Platform Partner");
            result.put("data", response);
            return result;
        } catch (Exception e) {
            log.error("Error searching hotels: {} - {}", e.getClass().getSimpleName(), e.getMessage(), e);
            Map<String, Object> result = new HashMap<>();
            result.put("status", "error");
            result.put("message", "Hotel service is currently unavailable. Error: " + e.getMessage());
            return result;
        }
    }

    @Schema(description = "Get detailed information about a specific hotel by its ID, including rooms, amenities, and pricing.")
    public static Map<String, Object> getHotelDetails(
            @Schema(description = "The unique ID of the hotel to retrieve details for") String hotelId) {
        try {
            log.info("Getting hotel details for ID: {}", hotelId);

            var response = ToolRegistry.getInstance().getHotelServiceClient()
                    .getHotelById(hotelId);

            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("source", "Platform Partner");
            result.put("data", response.getData());
            return result;
        } catch (Exception e) {
            log.error("Error getting hotel details: {}", e.getMessage());
            Map<String, Object> result = new HashMap<>();
            result.put("status", "error");
            result.put("message", "Could not retrieve hotel details. Hotel ID: " + hotelId);
            return result;
        }
    }
}
