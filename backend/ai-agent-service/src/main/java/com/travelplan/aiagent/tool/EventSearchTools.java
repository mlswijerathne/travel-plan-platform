package com.travelplan.aiagent.tool;

import com.google.adk.tools.Annotations.Schema;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class EventSearchTools {

    @Schema(description = "Search for events and activities registered on the platform in Sri Lanka. Returns platform-listed events filtered by location, category, and date range.")
    public static Map<String, Object> searchEvents(
            @Schema(description = "Location or city to search for events, e.g. 'Colombo', 'Kandy', 'Galle'") String location,
            @Schema(description = "Event category, e.g. 'CULTURAL', 'FESTIVAL', 'ADVENTURE', 'FOOD', 'MUSIC', 'SPORTS'. Pass empty string to search all.") String category,
            @Schema(description = "Start date filter in ISO format YYYY-MM-DD. Pass empty string to skip.") String dateFrom,
            @Schema(description = "End date filter in ISO format YYYY-MM-DD. Pass empty string to skip.") String dateTo) {
        try {
            log.info("Searching events - location: {}, category: {}, dateFrom: {}, dateTo: {}", location, category, dateFrom, dateTo);

            String locationParam = (location != null && !location.isBlank()) ? location : null;
            String categoryParam = (category != null && !category.isBlank()) ? category : null;
            String dateFromParam = (dateFrom != null && !dateFrom.isBlank()) ? dateFrom : null;
            String dateToParam = (dateTo != null && !dateTo.isBlank()) ? dateTo : null;

            Object response = ToolRegistry.getInstance().getEventServiceClient()
                    .browseEvents(categoryParam, locationParam, dateFromParam, dateToParam, null, null, 0, 10);

            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("source", "Platform Partner");
            result.put("data", response);
            return result;
        } catch (Exception e) {
            log.error("Error searching events: {} - {}", e.getClass().getSimpleName(), e.getMessage(), e);
            Map<String, Object> result = new HashMap<>();
            result.put("status", "error");
            result.put("message", "Event service is currently unavailable. Error: " + e.getMessage());
            return result;
        }
    }

    @Schema(description = "Get detailed information about a specific platform event by its ID.")
    public static Map<String, Object> getEventDetails(
            @Schema(description = "The unique numeric ID of the event to retrieve details for") long eventId) {
        try {
            log.info("Getting event details for ID: {}", eventId);

            Object response = ToolRegistry.getInstance().getEventServiceClient()
                    .getEventById(eventId);

            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("source", "Platform Partner");
            result.put("data", response);
            return result;
        } catch (Exception e) {
            log.error("Error getting event details: {}", e.getMessage());
            Map<String, Object> result = new HashMap<>();
            result.put("status", "error");
            result.put("message", "Could not retrieve event details. Event ID: " + eventId);
            return result;
        }
    }
}
