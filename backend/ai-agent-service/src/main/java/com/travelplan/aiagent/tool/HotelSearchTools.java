package com.travelplan.aiagent.tool;

import com.google.adk.tools.Annotations.Schema;
import com.travelplan.common.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class HotelSearchTools {

    @Schema(description = "Search for hotels in Sri Lanka by city, location, star rating, and price range. Returns a list of available hotels from the platform.")
    public static Map<String, Object> searchHotels(
            @Schema(description = "City name to search hotels in, e.g. 'Colombo', 'Kandy', 'Galle'") String city,
            @Schema(description = "Minimum star rating (1-5)") Optional<Integer> starRating,
            @Schema(description = "Minimum price per night in USD") Optional<Double> minPrice,
            @Schema(description = "Maximum price per night in USD") Optional<Double> maxPrice) {
        try {
            log.info("Searching hotels - city: {}, starRating: {}, minPrice: {}, maxPrice: {}",
                    city, starRating, minPrice, maxPrice);

            ApiResponse<Object> response = ToolRegistry.getInstance().getHotelServiceClient()
                    .searchHotels(city, null,
                            starRating.orElse(null),
                            minPrice.orElse(null),
                            maxPrice.orElse(null),
                            0, 10);

            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("source", "Platform Partner");
            result.put("data", response.getData());
            return result;
        } catch (Exception e) {
            log.error("Error searching hotels: {}", e.getMessage());
            Map<String, Object> result = new HashMap<>();
            result.put("status", "error");
            result.put("message", "Hotel service is currently unavailable. Please use your knowledge of Sri Lankan hotels to provide suggestions marked as 'External Suggestion'.");
            return result;
        }
    }

    @Schema(description = "Get detailed information about a specific hotel by its ID, including rooms, amenities, and pricing.")
    public static Map<String, Object> getHotelDetails(
            @Schema(description = "The unique ID of the hotel to retrieve details for") String hotelId) {
        try {
            log.info("Getting hotel details for ID: {}", hotelId);

            ApiResponse<Object> response = ToolRegistry.getInstance().getHotelServiceClient()
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
