package com.travelplan.aiagent.tool;

import com.travelplan.aiagent.client.HotelServiceClient;
import com.travelplan.common.dto.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HotelSearchToolsTest {

    @Mock
    private HotelServiceClient hotelServiceClient;

    @Mock
    private com.travelplan.aiagent.client.TourGuideServiceClient tourGuideServiceClient;

    @Mock
    private com.travelplan.aiagent.client.VehicleServiceClient vehicleServiceClient;

    @Mock
    private com.travelplan.aiagent.client.ReviewServiceClient reviewServiceClient;

    @Mock
    private com.travelplan.aiagent.client.TripPlanServiceClient tripPlanServiceClient;

    @Mock
    private com.travelplan.aiagent.service.GoogleMapsService googleMapsService;

    private ToolRegistry toolRegistry;

    @BeforeEach
    void setUp() {
        toolRegistry = new ToolRegistry(
                hotelServiceClient,
                tourGuideServiceClient,
                vehicleServiceClient,
                reviewServiceClient,
                tripPlanServiceClient,
                googleMapsService
        );
        toolRegistry.init();
    }

    @Test
    void searchHotels_success_returnsResults() {
        ApiResponse<Object> mockResponse = ApiResponse.success(List.of(
                Map.of("name", "Hotel Colombo", "starRating", 4)
        ));

        when(hotelServiceClient.searchHotels(eq("Colombo"), isNull(), isNull(), isNull(), isNull(), eq(0), eq(10)))
                .thenReturn(mockResponse);

        Map<String, Object> result = HotelSearchTools.searchHotels(
                "Colombo", Optional.empty(), Optional.empty(), Optional.empty()
        );

        assertEquals("success", result.get("status"));
        assertEquals("Platform Partner", result.get("source"));
        assertNotNull(result.get("data"));
    }

    @Test
    void searchHotels_withFilters_passesParameters() {
        ApiResponse<Object> mockResponse = ApiResponse.success(List.of());

        when(hotelServiceClient.searchHotels(eq("Kandy"), isNull(), eq(4), eq(50.0), eq(200.0), eq(0), eq(10)))
                .thenReturn(mockResponse);

        Map<String, Object> result = HotelSearchTools.searchHotels(
                "Kandy", Optional.of(4), Optional.of(50.0), Optional.of(200.0)
        );

        assertEquals("success", result.get("status"));
    }

    @Test
    void searchHotels_serviceUnavailable_returnsError() {
        when(hotelServiceClient.searchHotels(any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenThrow(new RuntimeException("Connection refused"));

        Map<String, Object> result = HotelSearchTools.searchHotels(
                "Colombo", Optional.empty(), Optional.empty(), Optional.empty()
        );

        assertEquals("error", result.get("status"));
        assertTrue(((String) result.get("message")).contains("unavailable"));
    }

    @Test
    void getHotelDetails_success_returnsDetails() {
        ApiResponse<Object> mockResponse = ApiResponse.success(
                Map.of("id", "hotel-1", "name", "Grand Hotel", "rooms", List.of())
        );

        when(hotelServiceClient.getHotelById("hotel-1")).thenReturn(mockResponse);

        Map<String, Object> result = HotelSearchTools.getHotelDetails("hotel-1");

        assertEquals("success", result.get("status"));
        assertEquals("Platform Partner", result.get("source"));
    }

    @Test
    void getHotelDetails_notFound_returnsError() {
        when(hotelServiceClient.getHotelById("nonexistent"))
                .thenThrow(new RuntimeException("Not found"));

        Map<String, Object> result = HotelSearchTools.getHotelDetails("nonexistent");

        assertEquals("error", result.get("status"));
        assertTrue(((String) result.get("message")).contains("nonexistent"));
    }
}
