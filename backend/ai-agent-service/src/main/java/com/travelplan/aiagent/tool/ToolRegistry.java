package com.travelplan.aiagent.tool;

import com.travelplan.aiagent.client.*;
import com.travelplan.aiagent.service.OpenStreetMapService;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Bridge between Spring DI and Google ADK's static tool methods.
 * ADK FunctionTool.create() requires static methods, but Feign clients need Spring DI.
 * This component self-registers in a static field on construction so that static tool
 * methods can access Feign clients and services via ToolRegistry.getInstance().
 */
@Slf4j
@Component
public class ToolRegistry {

    private static volatile ToolRegistry instance;

    @Getter
    private final HotelServiceClient hotelServiceClient;
    @Getter
    private final TourGuideServiceClient tourGuideServiceClient;
    @Getter
    private final VehicleServiceClient vehicleServiceClient;
    @Getter
    private final ReviewServiceClient reviewServiceClient;
    @Getter
    private final TripPlanServiceClient tripPlanServiceClient;
    @Getter
    private final EventServiceClient eventServiceClient;
    @Getter
    private final EcommerceServiceClient ecommerceServiceClient;
    @Getter
    private final OpenStreetMapService openStreetMapService;

    public ToolRegistry(HotelServiceClient hotelServiceClient,
                        TourGuideServiceClient tourGuideServiceClient,
                        VehicleServiceClient vehicleServiceClient,
                        ReviewServiceClient reviewServiceClient,
                        TripPlanServiceClient tripPlanServiceClient,
                        EventServiceClient eventServiceClient,
                        EcommerceServiceClient ecommerceServiceClient,
                        OpenStreetMapService openStreetMapService) {
        this.hotelServiceClient = hotelServiceClient;
        this.tourGuideServiceClient = tourGuideServiceClient;
        this.vehicleServiceClient = vehicleServiceClient;
        this.reviewServiceClient = reviewServiceClient;
        this.tripPlanServiceClient = tripPlanServiceClient;
        this.eventServiceClient = eventServiceClient;
        this.ecommerceServiceClient = ecommerceServiceClient;
        this.openStreetMapService = openStreetMapService;
    }

    @PostConstruct
    public void init() {
        instance = this;
        log.info("ToolRegistry initialized with all Feign clients and OpenStreetMapService (hotels, guides, vehicles, reviews, trip-plan, events, products)");
    }

    public static ToolRegistry getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ToolRegistry has not been initialized. Ensure Spring context is fully loaded.");
        }
        return instance;
    }
}
