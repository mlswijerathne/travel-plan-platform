package com.travelplan.aiagent.config;

import com.travelplan.aiagent.client.*;
import com.travelplan.common.dto.ApiResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.*;

/**
 * Mock Feign client implementations for dev testing without running other microservices.
 * Returns realistic Sri Lanka travel dummy data.
 */
@Configuration
@Profile("dev")
public class MockFeignClients {

    // ==================== HOTEL SERVICE ====================
    @Bean

    public HotelServiceClient mockHotelServiceClient() {
        return new HotelServiceClient() {
            private final List<Map<String, Object>> hotels = List.of(
                    Map.ofEntries(
                            Map.entry("id", "h1"),
                            Map.entry("name", "Shangri-La Colombo"),
                            Map.entry("description", "Luxury 5-star hotel overlooking the Indian Ocean with world-class dining and spa."),
                            Map.entry("city", "Colombo"),
                            Map.entry("address", "1 Galle Face Center Road, Colombo 02"),
                            Map.entry("starRating", 5),
                            Map.entry("rating", 4.8),
                            Map.entry("reviewCount", 342),
                            Map.entry("pricePerNight", 250.0),
                            Map.entry("amenities", List.of("Pool", "Spa", "WiFi", "Restaurant", "Gym", "Ocean View")),
                            Map.entry("imageUrl", "https://images.unsplash.com/photo-1566073771259-6a8506099945"),
                            Map.entry("latitude", 6.9271),
                            Map.entry("longitude", 79.8612)
                    ),
                    Map.ofEntries(
                            Map.entry("id", "h2"),
                            Map.entry("name", "The Kandy House"),
                            Map.entry("description", "Boutique colonial-era manor surrounded by lush gardens in the hills of Kandy."),
                            Map.entry("city", "Kandy"),
                            Map.entry("address", "Amunugama, Gunnepana, Kandy"),
                            Map.entry("starRating", 4),
                            Map.entry("rating", 4.6),
                            Map.entry("reviewCount", 189),
                            Map.entry("pricePerNight", 120.0),
                            Map.entry("amenities", List.of("Pool", "WiFi", "Restaurant", "Garden", "Heritage")),
                            Map.entry("imageUrl", "https://images.unsplash.com/photo-1520250497591-112f2f40a3f4"),
                            Map.entry("latitude", 7.2906),
                            Map.entry("longitude", 80.6337)
                    ),
                    Map.ofEntries(
                            Map.entry("id", "h3"),
                            Map.entry("name", "Jetwing Lighthouse"),
                            Map.entry("description", "Iconic Geoffrey Bawa-designed hotel perched on a rocky headland near Galle Fort."),
                            Map.entry("city", "Galle"),
                            Map.entry("address", "Dadella, Galle"),
                            Map.entry("starRating", 5),
                            Map.entry("rating", 4.7),
                            Map.entry("reviewCount", 276),
                            Map.entry("pricePerNight", 180.0),
                            Map.entry("amenities", List.of("Pool", "Spa", "WiFi", "Restaurant", "Beach Access", "Bar")),
                            Map.entry("imageUrl", "https://images.unsplash.com/photo-1571896349842-33c89424de2d"),
                            Map.entry("latitude", 6.0174),
                            Map.entry("longitude", 80.2222)
                    ),
                    Map.ofEntries(
                            Map.entry("id", "h4"),
                            Map.entry("name", "Heritance Tea Factory"),
                            Map.entry("description", "Converted tea factory hotel in Nuwara Eliya offering panoramic views of tea plantations."),
                            Map.entry("city", "Nuwara Eliya"),
                            Map.entry("address", "Kandapola, Nuwara Eliya"),
                            Map.entry("starRating", 4),
                            Map.entry("rating", 4.5),
                            Map.entry("reviewCount", 215),
                            Map.entry("pricePerNight", 95.0),
                            Map.entry("amenities", List.of("WiFi", "Restaurant", "Tea Tasting", "Hiking", "Scenic Views")),
                            Map.entry("imageUrl", "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb"),
                            Map.entry("latitude", 6.9497),
                            Map.entry("longitude", 80.7891)
                    ),
                    Map.ofEntries(
                            Map.entry("id", "h5"),
                            Map.entry("name", "Cinnamon Wild Yala"),
                            Map.entry("description", "Safari-style eco lodge bordering Yala National Park, perfect for wildlife enthusiasts."),
                            Map.entry("city", "Yala"),
                            Map.entry("address", "Kirinda Road, Yala"),
                            Map.entry("starRating", 4),
                            Map.entry("rating", 4.4),
                            Map.entry("reviewCount", 156),
                            Map.entry("pricePerNight", 140.0),
                            Map.entry("amenities", List.of("Pool", "WiFi", "Restaurant", "Safari Tours", "Nature Walks")),
                            Map.entry("imageUrl", "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b"),
                            Map.entry("latitude", 6.3728),
                            Map.entry("longitude", 81.3789)
                    ),
                    Map.ofEntries(
                            Map.entry("id", "h6"),
                            Map.entry("name", "Anantara Peace Haven Tangalle"),
                            Map.entry("description", "Beachfront luxury resort with private villas on Sri Lanka's pristine southern coast."),
                            Map.entry("city", "Tangalle"),
                            Map.entry("address", "Goyambokka Estate, Tangalle"),
                            Map.entry("starRating", 5),
                            Map.entry("rating", 4.9),
                            Map.entry("reviewCount", 198),
                            Map.entry("pricePerNight", 300.0),
                            Map.entry("amenities", List.of("Private Pool", "Spa", "WiFi", "Beach", "Fine Dining", "Yoga")),
                            Map.entry("imageUrl", "https://images.unsplash.com/photo-1564501049412-61c2a3083791"),
                            Map.entry("latitude", 6.0267),
                            Map.entry("longitude", 80.7958)
                    ),
                    Map.ofEntries(
                            Map.entry("id", "h7"),
                            Map.entry("name", "Ella Flower Garden Resort"),
                            Map.entry("description", "Budget-friendly hillside retreat with stunning views of Ella Gap and surrounding mountains."),
                            Map.entry("city", "Ella"),
                            Map.entry("address", "Passara Road, Ella"),
                            Map.entry("starRating", 3),
                            Map.entry("rating", 4.3),
                            Map.entry("reviewCount", 324),
                            Map.entry("pricePerNight", 45.0),
                            Map.entry("amenities", List.of("WiFi", "Restaurant", "Mountain View", "Garden", "Trekking")),
                            Map.entry("imageUrl", "https://images.unsplash.com/photo-1551882547-ff40c63fe5fa"),
                            Map.entry("latitude", 6.8667),
                            Map.entry("longitude", 81.0466)
                    )
            );

            @Override
            public ApiResponse<Object> searchHotels(String city, String location, Integer starRating,
                                                    Double minPrice, Double maxPrice, int page, int size) {
                var filtered = hotels.stream()
                        .filter(h -> city == null || h.get("city").toString().toLowerCase().contains(city.toLowerCase()))
                        .filter(h -> starRating == null || (int) h.get("starRating") >= starRating)
                        .filter(h -> minPrice == null || (double) h.get("pricePerNight") >= minPrice)
                        .filter(h -> maxPrice == null || (double) h.get("pricePerNight") <= maxPrice)
                        .toList();
                return ApiResponse.success(filtered);
            }

            @Override
            public ApiResponse<Object> getHotelById(String id) {
                return hotels.stream().filter(h -> h.get("id").equals(id)).findFirst()
                        .<Object>map(h -> h)
                        .map(ApiResponse::success)
                        .orElse(ApiResponse.success(null));
            }

            @Override
            public ApiResponse<Object> searchHotelsByQuery(Map<String, String> params) {
                return searchHotels(params.get("city"), params.get("location"), null, null, null, 0, 10);
            }
        };
    }

    // ==================== TOUR GUIDE SERVICE ====================
    @Bean

    public TourGuideServiceClient mockTourGuideServiceClient() {
        return new TourGuideServiceClient() {
            private final List<Map<String, Object>> guides = List.of(
                    Map.ofEntries(
                            Map.entry("id", "g1"),
                            Map.entry("name", "Samantha Perera"),
                            Map.entry("bio", "Certified national guide with 12 years experience specializing in cultural and historical tours across Sri Lanka."),
                            Map.entry("location", "Colombo"),
                            Map.entry("serviceAreas", List.of("Colombo", "Kandy", "Sigiriya", "Anuradhapura")),
                            Map.entry("languages", List.of("English", "Sinhala", "Tamil")),
                            Map.entry("specializations", List.of("History", "Culture", "Architecture")),
                            Map.entry("hourlyRate", 25.0),
                            Map.entry("rating", 4.9),
                            Map.entry("reviewCount", 187),
                            Map.entry("imageUrl", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d")
                    ),
                    Map.ofEntries(
                            Map.entry("id", "g2"),
                            Map.entry("name", "Dinesh Fernando"),
                            Map.entry("bio", "Wildlife photography expert and safari guide. Knows every trail in Yala and Wilpattu national parks."),
                            Map.entry("location", "Yala"),
                            Map.entry("serviceAreas", List.of("Yala", "Wilpattu", "Udawalawe", "Minneriya")),
                            Map.entry("languages", List.of("English", "Sinhala")),
                            Map.entry("specializations", List.of("Wildlife", "Photography", "Nature")),
                            Map.entry("hourlyRate", 30.0),
                            Map.entry("rating", 4.8),
                            Map.entry("reviewCount", 143),
                            Map.entry("imageUrl", "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e")
                    ),
                    Map.ofEntries(
                            Map.entry("id", "g3"),
                            Map.entry("name", "Ayesha Silva"),
                            Map.entry("bio", "Passionate food and culinary tour guide bringing you the best authentic Sri Lankan cuisine experiences."),
                            Map.entry("location", "Galle"),
                            Map.entry("serviceAreas", List.of("Galle", "Colombo", "Matara", "Tangalle")),
                            Map.entry("languages", List.of("English", "Sinhala", "French")),
                            Map.entry("specializations", List.of("Food", "Culture", "Walking Tours")),
                            Map.entry("hourlyRate", 20.0),
                            Map.entry("rating", 4.7),
                            Map.entry("reviewCount", 98),
                            Map.entry("imageUrl", "https://images.unsplash.com/photo-1438761681033-6461ffad8d80")
                    ),
                    Map.ofEntries(
                            Map.entry("id", "g4"),
                            Map.entry("name", "Kasun Jayawardena"),
                            Map.entry("bio", "Adventure guide specializing in hiking, trekking, and white-water rafting in Sri Lanka's hill country."),
                            Map.entry("location", "Ella"),
                            Map.entry("serviceAreas", List.of("Ella", "Nuwara Eliya", "Knuckles Range", "Adam's Peak")),
                            Map.entry("languages", List.of("English", "Sinhala", "German")),
                            Map.entry("specializations", List.of("Adventure", "Hiking", "Nature")),
                            Map.entry("hourlyRate", 22.0),
                            Map.entry("rating", 4.6),
                            Map.entry("reviewCount", 112),
                            Map.entry("imageUrl", "https://images.unsplash.com/photo-1500648767791-00dcc994a43e")
                    )
            );

            @Override
            public ApiResponse<Object> searchTourGuides(String location, String language,
                                                        String specialization, int page, int size) {
                var filtered = guides.stream()
                        .filter(g -> location == null || g.get("location").toString().toLowerCase().contains(location.toLowerCase())
                                || ((List<?>) g.get("serviceAreas")).stream().anyMatch(a -> a.toString().toLowerCase().contains(location.toLowerCase())))
                        .filter(g -> language == null || ((List<?>) g.get("languages")).stream().anyMatch(l -> l.toString().toLowerCase().contains(language.toLowerCase())))
                        .filter(g -> specialization == null || ((List<?>) g.get("specializations")).stream().anyMatch(s -> s.toString().toLowerCase().contains(specialization.toLowerCase())))
                        .toList();
                return ApiResponse.success(filtered);
            }

            @Override
            public ApiResponse<Object> getTourGuideById(String id) {
                return guides.stream().filter(g -> g.get("id").equals(id)).findFirst()
                        .<Object>map(g -> g)
                        .map(ApiResponse::success)
                        .orElse(ApiResponse.success(null));
            }

            @Override
            public ApiResponse<Object> searchTourGuidesByQuery(Map<String, String> params) {
                return searchTourGuides(params.get("location"), params.get("language"), params.get("specialization"), 0, 10);
            }
        };
    }

    // ==================== VEHICLE SERVICE ====================
    @Bean

    public VehicleServiceClient mockVehicleServiceClient() {
        return new VehicleServiceClient() {
            private final List<Map<String, Object>> vehicles = List.of(
                    Map.ofEntries(
                            Map.entry("id", "v1"),
                            Map.entry("type", "SUV"),
                            Map.entry("make", "Toyota"),
                            Map.entry("model", "Land Cruiser"),
                            Map.entry("year", 2023),
                            Map.entry("capacity", 7),
                            Map.entry("location", "Colombo"),
                            Map.entry("dailyRate", 85.0),
                            Map.entry("features", List.of("AC", "GPS", "WiFi", "Luggage Rack")),
                            Map.entry("rating", 4.7),
                            Map.entry("reviewCount", 89),
                            Map.entry("imageUrl", "https://images.unsplash.com/photo-1519641471654-76ce0107ad1b")
                    ),
                    Map.ofEntries(
                            Map.entry("id", "v2"),
                            Map.entry("type", "Van"),
                            Map.entry("make", "Toyota"),
                            Map.entry("model", "KDH"),
                            Map.entry("year", 2022),
                            Map.entry("capacity", 12),
                            Map.entry("location", "Colombo"),
                            Map.entry("dailyRate", 65.0),
                            Map.entry("features", List.of("AC", "WiFi", "Luggage Space")),
                            Map.entry("rating", 4.5),
                            Map.entry("reviewCount", 134),
                            Map.entry("imageUrl", "https://images.unsplash.com/photo-1570125909232-eb263c188f7e")
                    ),
                    Map.ofEntries(
                            Map.entry("id", "v3"),
                            Map.entry("type", "Car"),
                            Map.entry("make", "Toyota"),
                            Map.entry("model", "Prius"),
                            Map.entry("year", 2024),
                            Map.entry("capacity", 4),
                            Map.entry("location", "Colombo"),
                            Map.entry("dailyRate", 40.0),
                            Map.entry("features", List.of("AC", "GPS", "Hybrid")),
                            Map.entry("rating", 4.6),
                            Map.entry("reviewCount", 201),
                            Map.entry("imageUrl", "https://images.unsplash.com/photo-1549317661-bd32c8ce0afe")
                    ),
                    Map.ofEntries(
                            Map.entry("id", "v4"),
                            Map.entry("type", "TukTuk"),
                            Map.entry("make", "Bajaj"),
                            Map.entry("model", "RE"),
                            Map.entry("year", 2023),
                            Map.entry("capacity", 3),
                            Map.entry("location", "Galle"),
                            Map.entry("dailyRate", 15.0),
                            Map.entry("features", List.of("Open Air", "City Tours")),
                            Map.entry("rating", 4.8),
                            Map.entry("reviewCount", 267),
                            Map.entry("imageUrl", "https://images.unsplash.com/photo-1612872087720-bb876e2e67d1")
                    ),
                    Map.ofEntries(
                            Map.entry("id", "v5"),
                            Map.entry("type", "Bus"),
                            Map.entry("make", "Ashok Leyland"),
                            Map.entry("model", "Lynx"),
                            Map.entry("year", 2022),
                            Map.entry("capacity", 30),
                            Map.entry("location", "Kandy"),
                            Map.entry("dailyRate", 150.0),
                            Map.entry("features", List.of("AC", "WiFi", "Reclining Seats", "Luggage Compartment")),
                            Map.entry("rating", 4.3),
                            Map.entry("reviewCount", 45),
                            Map.entry("imageUrl", "https://images.unsplash.com/photo-1544620347-c4fd4a3d5957")
                    )
            );

            @Override
            public ApiResponse<Object> searchVehicles(String type, String location,
                                                      Double minPrice, Double maxPrice, int page, int size) {
                var filtered = vehicles.stream()
                        .filter(v -> type == null || v.get("type").toString().equalsIgnoreCase(type))
                        .filter(v -> location == null || v.get("location").toString().toLowerCase().contains(location.toLowerCase()))
                        .filter(v -> minPrice == null || (double) v.get("dailyRate") >= minPrice)
                        .filter(v -> maxPrice == null || (double) v.get("dailyRate") <= maxPrice)
                        .toList();
                return ApiResponse.success(filtered);
            }

            @Override
            public ApiResponse<Object> getVehicleById(String id) {
                return vehicles.stream().filter(v -> v.get("id").equals(id)).findFirst()
                        .<Object>map(v -> v)
                        .map(ApiResponse::success)
                        .orElse(ApiResponse.success(null));
            }

            @Override
            public ApiResponse<Object> searchVehiclesByQuery(Map<String, String> params) {
                return searchVehicles(params.get("type"), params.get("location"), null, null, 0, 10);
            }
        };
    }

    // ==================== REVIEW SERVICE ====================
    @Bean

    public ReviewServiceClient mockReviewServiceClient() {
        return new ReviewServiceClient() {
            private final Map<String, List<Map<String, Object>>> reviews = Map.of(
                    "h1", List.of(
                            Map.of("id", "r1", "rating", 5, "text", "Absolutely stunning hotel with incredible ocean views. The staff went above and beyond!", "author", "John T.", "date", "2026-01-15"),
                            Map.of("id", "r2", "rating", 5, "text", "Best luxury hotel in Colombo. The rooftop pool is amazing at sunset.", "author", "Sarah M.", "date", "2026-01-10")
                    ),
                    "h2", List.of(
                            Map.of("id", "r3", "rating", 4, "text", "Beautiful colonial property with great ambiance. Perfect for a quiet retreat in Kandy.", "author", "Mike R.", "date", "2026-01-20")
                    ),
                    "g1", List.of(
                            Map.of("id", "r4", "rating", 5, "text", "Samantha was incredible! So knowledgeable about Sri Lankan history and very friendly.", "author", "Emma L.", "date", "2026-02-01"),
                            Map.of("id", "r5", "rating", 5, "text", "Best tour guide we've ever had. Made the ancient cities come alive!", "author", "David K.", "date", "2026-01-25")
                    ),
                    "v1", List.of(
                            Map.of("id", "r6", "rating", 5, "text", "Clean, comfortable SUV. Driver was excellent and very knowledgeable about routes.", "author", "Anna P.", "date", "2026-02-05")
                    )
            );

            @Override
            public ApiResponse<Object> getReviews(String entityType, String entityId, int page, int size) {
                var entityReviews = reviews.getOrDefault(entityId, List.of());
                return ApiResponse.success(entityReviews);
            }

            @Override
            public ApiResponse<Object> getReviewSummary(String entityType, String entityId) {
                var entityReviews = reviews.getOrDefault(entityId, List.of());
                double avgRating = entityReviews.stream()
                        .mapToInt(r -> (int) r.get("rating"))
                        .average().orElse(0.0);
                Map<String, Object> summary = new HashMap<>();
                summary.put("averageRating", Math.round(avgRating * 10.0) / 10.0);
                summary.put("totalReviews", entityReviews.size());
                summary.put("entityType", entityType);
                summary.put("entityId", entityId);
                return ApiResponse.success(summary);
            }
        };
    }

    // ==================== TRIP PLAN SERVICE ====================
    @Bean

    public TripPlanServiceClient mockTripPlanServiceClient() {
        return new TripPlanServiceClient() {
            private final List<Map<String, Object>> packages = List.of(
                    Map.ofEntries(
                            Map.entry("id", "p1"),
                            Map.entry("name", "Cultural Triangle Explorer"),
                            Map.entry("description", "7-day journey through Sri Lanka's UNESCO World Heritage sites including Sigiriya, Dambulla, Polonnaruwa, and Anuradhapura."),
                            Map.entry("destination", "Central & North Central"),
                            Map.entry("duration", 7),
                            Map.entry("price", 850.0),
                            Map.entry("theme", "Culture"),
                            Map.entry("rating", 4.8),
                            Map.entry("includes", List.of("Hotel accommodation", "Private transport", "Tour guide", "Entrance fees", "Breakfast")),
                            Map.entry("imageUrl", "https://images.unsplash.com/photo-1588416936097-41850ab3d86d")
                    ),
                    Map.ofEntries(
                            Map.entry("id", "p2"),
                            Map.entry("name", "Southern Beach Escape"),
                            Map.entry("description", "5-day beach holiday along Sri Lanka's stunning southern coast from Galle to Tangalle."),
                            Map.entry("destination", "Southern Coast"),
                            Map.entry("duration", 5),
                            Map.entry("price", 600.0),
                            Map.entry("theme", "Beach"),
                            Map.entry("rating", 4.7),
                            Map.entry("includes", List.of("Beachfront hotels", "Private car", "Surfing lesson", "Whale watching", "Breakfast & dinner")),
                            Map.entry("imageUrl", "https://images.unsplash.com/photo-1507525428034-b723cf961d3e")
                    ),
                    Map.ofEntries(
                            Map.entry("id", "p3"),
                            Map.entry("name", "Wildlife Safari Adventure"),
                            Map.entry("description", "4-day wildlife experience covering Yala and Udawalawe national parks with expert naturalist guides."),
                            Map.entry("destination", "Yala & Udawalawe"),
                            Map.entry("duration", 4),
                            Map.entry("price", 520.0),
                            Map.entry("theme", "Wildlife"),
                            Map.entry("rating", 4.9),
                            Map.entry("includes", List.of("Safari lodge", "Jeep safaris", "Naturalist guide", "All meals", "Park fees")),
                            Map.entry("imageUrl", "https://images.unsplash.com/photo-1516426122078-c23e76319801")
                    ),
                    Map.ofEntries(
                            Map.entry("id", "p4"),
                            Map.entry("name", "Hill Country Tea Trail"),
                            Map.entry("description", "3-day scenic journey through tea plantations, waterfalls, and misty mountains from Kandy to Ella."),
                            Map.entry("destination", "Hill Country"),
                            Map.entry("duration", 3),
                            Map.entry("price", 350.0),
                            Map.entry("theme", "Nature"),
                            Map.entry("rating", 4.6),
                            Map.entry("includes", List.of("Boutique hotels", "Train ride", "Tea factory tour", "Hiking guide", "Breakfast")),
                            Map.entry("imageUrl", "https://images.unsplash.com/photo-1587974928442-77dc3e0dba72")
                    )
            );

            @Override
            public ApiResponse<Object> searchPackages(String destination, Integer duration,
                                                      Double minBudget, Double maxBudget, int page, int size) {
                var filtered = packages.stream()
                        .filter(p -> destination == null || p.get("destination").toString().toLowerCase().contains(destination.toLowerCase())
                                || p.get("name").toString().toLowerCase().contains(destination.toLowerCase()))
                        .filter(p -> duration == null || (int) p.get("duration") <= duration + 1)
                        .filter(p -> minBudget == null || (double) p.get("price") >= minBudget)
                        .filter(p -> maxBudget == null || (double) p.get("price") <= maxBudget)
                        .toList();
                return ApiResponse.success(filtered);
            }

            @Override
            public ApiResponse<Object> getPackageById(String id) {
                return packages.stream().filter(p -> p.get("id").equals(id)).findFirst()
                        .<Object>map(p -> p)
                        .map(ApiResponse::success)
                        .orElse(ApiResponse.success(null));
            }

            @Override
            public ApiResponse<Object> searchPackagesByQuery(Map<String, String> params) {
                return searchPackages(params.get("destination"), null, null, null, 0, 10);
            }
        };
    }
}
