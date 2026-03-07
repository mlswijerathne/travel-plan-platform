package com.travelplan.aiagent.config;

import com.travelplan.aiagent.client.*;
import com.travelplan.common.dto.ApiResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;
import java.util.*;

/**
 * Mock Feign client implementations for dev testing without running other microservices.
 * Returns realistic Sri Lanka travel dummy data.
 */
@Configuration
@Profile("dev")
public class MockFeignClients {

    private static boolean matchesQuery(Map<String, Object> guide, String query) {
        String q = query.toLowerCase();
        return guide.get("firstName").toString().toLowerCase().contains(q)
                || guide.get("lastName").toString().toLowerCase().contains(q)
                || guide.get("bio").toString().toLowerCase().contains(q)
                || ((List<?>) guide.get("serviceAreas")).stream()
                        .anyMatch(a -> a.toString().toLowerCase().contains(q));
    }

    // ==================== HOTEL SERVICE ====================
    @Bean
    public HotelServiceClient mockHotelServiceClient() {
        return new HotelServiceClient() {
            private final List<Map<String, Object>> hotels = List.of(
                    Map.ofEntries(
                            Map.entry("id", 1L),
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
                            Map.entry("id", 2L),
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
                            Map.entry("id", 3L),
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
                            Map.entry("id", 4L),
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
                            Map.entry("id", 5L),
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
                            Map.entry("id", 6L),
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
                            Map.entry("id", 7L),
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
            public Object searchHotels(String city, Integer starRating, int page, int size) {
                var filtered = hotels.stream()
                        .filter(h -> city == null || h.get("city").toString().toLowerCase().contains(city.toLowerCase()))
                        .filter(h -> starRating == null || (int) h.get("starRating") >= starRating)
                        .toList();
                Map<String, Object> result = new LinkedHashMap<>();
                result.put("content", filtered);
                result.put("page", page);
                result.put("size", size);
                result.put("totalElements", filtered.size());
                return result;
            }

            @Override
            public ApiResponse<Object> getHotelById(String id) {
                return hotels.stream()
                        .filter(h -> String.valueOf(h.get("id")).equals(id))
                        .findFirst()
                        .<Object>map(h -> h)
                        .map(ApiResponse::success)
                        .orElse(ApiResponse.success(null));
            }

            @Override
            public Object searchHotelsByQuery(String q, int page, int size) {
                var filtered = hotels.stream()
                        .filter(h -> q == null || h.get("name").toString().toLowerCase().contains(q.toLowerCase())
                                || h.get("city").toString().toLowerCase().contains(q.toLowerCase())
                                || h.get("description").toString().toLowerCase().contains(q.toLowerCase()))
                        .toList();
                Map<String, Object> result = new LinkedHashMap<>();
                result.put("content", filtered);
                result.put("page", page);
                result.put("size", size);
                result.put("totalElements", filtered.size());
                return result;
            }
        };
    }

    // ==================== TOUR GUIDE SERVICE ====================
    @Bean
    public TourGuideServiceClient mockTourGuideServiceClient() {
        return new TourGuideServiceClient() {
            private final List<Map<String, Object>> guides = List.of(
                    Map.ofEntries(
                            Map.entry("id", 1L),
                            Map.entry("firstName", "Samantha"),
                            Map.entry("lastName", "Perera"),
                            Map.entry("bio", "Certified national guide based in Colombo with 12 years experience specializing in cultural and historical tours across Sri Lanka."),
                            Map.entry("serviceAreas", List.of("Colombo", "Kandy", "Sigiriya", "Anuradhapura")),
                            Map.entry("languages", List.of("English", "Sinhala", "Tamil")),
                            Map.entry("specializations", List.of("History", "Culture", "Architecture")),
                            Map.entry("hourlyRate", 25.0),
                            Map.entry("dailyRate", 150.0),
                            Map.entry("rating", 4.9),
                            Map.entry("reviewCount", 187),
                            Map.entry("isVerified", true),
                            Map.entry("imageUrl", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d")
                    ),
                    Map.ofEntries(
                            Map.entry("id", 2L),
                            Map.entry("firstName", "Dinesh"),
                            Map.entry("lastName", "Fernando"),
                            Map.entry("bio", "Wildlife photography expert and safari guide in Yala. Knows every trail in Yala and Wilpattu national parks."),
                            Map.entry("serviceAreas", List.of("Yala", "Wilpattu", "Udawalawe", "Minneriya")),
                            Map.entry("languages", List.of("English", "Sinhala")),
                            Map.entry("specializations", List.of("Wildlife", "Photography", "Nature")),
                            Map.entry("hourlyRate", 30.0),
                            Map.entry("dailyRate", 180.0),
                            Map.entry("rating", 4.8),
                            Map.entry("reviewCount", 143),
                            Map.entry("isVerified", true),
                            Map.entry("imageUrl", "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e")
                    ),
                    Map.ofEntries(
                            Map.entry("id", 3L),
                            Map.entry("firstName", "Ayesha"),
                            Map.entry("lastName", "Silva"),
                            Map.entry("bio", "Passionate food and culinary tour guide in Galle bringing you the best authentic Sri Lankan cuisine experiences."),
                            Map.entry("serviceAreas", List.of("Galle", "Colombo", "Matara", "Tangalle")),
                            Map.entry("languages", List.of("English", "Sinhala", "French")),
                            Map.entry("specializations", List.of("Food", "Culture", "Walking Tours")),
                            Map.entry("hourlyRate", 20.0),
                            Map.entry("dailyRate", 120.0),
                            Map.entry("rating", 4.7),
                            Map.entry("reviewCount", 98),
                            Map.entry("isVerified", true),
                            Map.entry("imageUrl", "https://images.unsplash.com/photo-1438761681033-6461ffad8d80")
                    ),
                    Map.ofEntries(
                            Map.entry("id", 4L),
                            Map.entry("firstName", "Kasun"),
                            Map.entry("lastName", "Jayawardena"),
                            Map.entry("bio", "Adventure guide specializing in hiking, trekking in Ella and white-water rafting in Sri Lanka's hill country."),
                            Map.entry("serviceAreas", List.of("Ella", "Nuwara Eliya", "Knuckles Range", "Adam's Peak")),
                            Map.entry("languages", List.of("English", "Sinhala", "German")),
                            Map.entry("specializations", List.of("Adventure", "Hiking", "Nature")),
                            Map.entry("hourlyRate", 22.0),
                            Map.entry("dailyRate", 130.0),
                            Map.entry("rating", 4.6),
                            Map.entry("reviewCount", 112),
                            Map.entry("isVerified", true),
                            Map.entry("imageUrl", "https://images.unsplash.com/photo-1500648767791-00dcc994a43e")
                    )
            );

            @Override
            public Object searchTourGuides(String language, String specialization,
                                           String query, int page, int size) {
                var filtered = guides.stream()
                        .filter(g -> language == null || ((List<?>) g.get("languages")).stream()
                                .anyMatch(l -> l.toString().toLowerCase().contains(language.toLowerCase())))
                        .filter(g -> specialization == null || ((List<?>) g.get("specializations")).stream()
                                .anyMatch(s -> s.toString().toLowerCase().contains(specialization.toLowerCase())))
                        .filter(g -> query == null || matchesQuery(g, query))
                        .toList();
                Map<String, Object> result = new LinkedHashMap<>();
                result.put("content", filtered);
                result.put("page", page);
                result.put("size", size);
                result.put("totalElements", filtered.size());
                return result;
            }

            @Override
            public ApiResponse<Object> getTourGuideById(String id) {
                return guides.stream()
                        .filter(g -> String.valueOf(g.get("id")).equals(id))
                        .findFirst()
                        .<Object>map(g -> g)
                        .map(ApiResponse::success)
                        .orElse(ApiResponse.success(null));
            }
        };
    }

    // ==================== VEHICLE SERVICE ====================
    @Bean
    public VehicleServiceClient mockVehicleServiceClient() {
        return new VehicleServiceClient() {
            private final List<Map<String, Object>> vehicles = List.of(
                    Map.ofEntries(
                            Map.entry("id", 1L),
                            Map.entry("vehicleType", "SUV"),
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
                            Map.entry("id", 2L),
                            Map.entry("vehicleType", "Van"),
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
                            Map.entry("id", 3L),
                            Map.entry("vehicleType", "Car"),
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
                            Map.entry("id", 4L),
                            Map.entry("vehicleType", "TukTuk"),
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
                            Map.entry("id", 5L),
                            Map.entry("vehicleType", "Bus"),
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
            public Object searchVehicles(String vehicleType, BigDecimal minDailyRate,
                                         BigDecimal maxDailyRate, String query, int page, int size) {
                var filtered = vehicles.stream()
                        .filter(v -> vehicleType == null || v.get("vehicleType").toString().equalsIgnoreCase(vehicleType))
                        .filter(v -> minDailyRate == null || (double) v.get("dailyRate") >= minDailyRate.doubleValue())
                        .filter(v -> maxDailyRate == null || (double) v.get("dailyRate") <= maxDailyRate.doubleValue())
                        .filter(v -> query == null || v.get("make").toString().toLowerCase().contains(query.toLowerCase())
                                || v.get("model").toString().toLowerCase().contains(query.toLowerCase())
                                || v.get("location").toString().toLowerCase().contains(query.toLowerCase()))
                        .toList();
                Map<String, Object> result = new LinkedHashMap<>();
                result.put("content", filtered);
                result.put("page", page);
                result.put("size", size);
                result.put("totalElements", filtered.size());
                return result;
            }

            @Override
            public Object getVehicleById(String id) {
                return vehicles.stream()
                        .filter(v -> String.valueOf(v.get("id")).equals(id))
                        .findFirst()
                        .<Object>map(v -> v)
                        .orElse(null);
            }
        };
    }

    // ==================== REVIEW SERVICE ====================
    @Bean
    public ReviewServiceClient mockReviewServiceClient() {
        return new ReviewServiceClient() {
            private final Map<String, List<Map<String, Object>>> reviews = Map.of(
                    "1", List.of(
                            Map.of("id", "r1", "rating", 5, "text", "Absolutely stunning hotel with incredible ocean views. The staff went above and beyond!", "author", "John T.", "date", "2026-01-15"),
                            Map.of("id", "r2", "rating", 5, "text", "Best luxury hotel in Colombo. The rooftop pool is amazing at sunset.", "author", "Sarah M.", "date", "2026-01-10")
                    ),
                    "2", List.of(
                            Map.of("id", "r3", "rating", 4, "text", "Beautiful colonial property with great ambiance. Perfect for a quiet retreat in Kandy.", "author", "Mike R.", "date", "2026-01-20")
                    ),
                    "3", List.of(
                            Map.of("id", "r4", "rating", 5, "text", "Samantha was incredible! So knowledgeable about Sri Lankan history and very friendly.", "author", "Emma L.", "date", "2026-02-01"),
                            Map.of("id", "r5", "rating", 5, "text", "Best tour guide we've ever had. Made the ancient cities come alive!", "author", "David K.", "date", "2026-01-25")
                    ),
                    "4", List.of(
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
                            Map.entry("id", 1L),
                            Map.entry("name", "Cultural Triangle Explorer"),
                            Map.entry("description", "7-day journey through Sri Lanka's UNESCO World Heritage sites including Sigiriya, Dambulla, Polonnaruwa, and Anuradhapura."),
                            Map.entry("destinations", List.of("Central", "North Central", "Sigiriya", "Kandy")),
                            Map.entry("durationDays", 7),
                            Map.entry("basePrice", 850.0),
                            Map.entry("finalPrice", 850.0),
                            Map.entry("rating", 4.8),
                            Map.entry("inclusions", List.of("Hotel accommodation", "Private transport", "Tour guide", "Entrance fees", "Breakfast")),
                            Map.entry("isFeatured", true),
                            Map.entry("imageUrl", "https://images.unsplash.com/photo-1588416936097-41850ab3d86d")
                    ),
                    Map.ofEntries(
                            Map.entry("id", 2L),
                            Map.entry("name", "Southern Beach Escape"),
                            Map.entry("description", "5-day beach holiday along Sri Lanka's stunning southern coast from Galle to Tangalle."),
                            Map.entry("destinations", List.of("Southern Coast", "Galle", "Tangalle", "Mirissa")),
                            Map.entry("durationDays", 5),
                            Map.entry("basePrice", 600.0),
                            Map.entry("finalPrice", 600.0),
                            Map.entry("rating", 4.7),
                            Map.entry("inclusions", List.of("Beachfront hotels", "Private car", "Surfing lesson", "Whale watching", "Breakfast & dinner")),
                            Map.entry("isFeatured", true),
                            Map.entry("imageUrl", "https://images.unsplash.com/photo-1507525428034-b723cf961d3e")
                    ),
                    Map.ofEntries(
                            Map.entry("id", 3L),
                            Map.entry("name", "Wildlife Safari Adventure"),
                            Map.entry("description", "4-day wildlife experience covering Yala and Udawalawe national parks with expert naturalist guides."),
                            Map.entry("destinations", List.of("Yala", "Udawalawe")),
                            Map.entry("durationDays", 4),
                            Map.entry("basePrice", 520.0),
                            Map.entry("finalPrice", 520.0),
                            Map.entry("rating", 4.9),
                            Map.entry("inclusions", List.of("Safari lodge", "Jeep safaris", "Naturalist guide", "All meals", "Park fees")),
                            Map.entry("isFeatured", true),
                            Map.entry("imageUrl", "https://images.unsplash.com/photo-1516426122078-c23e76319801")
                    ),
                    Map.ofEntries(
                            Map.entry("id", 4L),
                            Map.entry("name", "Hill Country Tea Trail"),
                            Map.entry("description", "3-day scenic journey through tea plantations, waterfalls, and misty mountains from Kandy to Ella."),
                            Map.entry("destinations", List.of("Hill Country", "Kandy", "Ella", "Nuwara Eliya")),
                            Map.entry("durationDays", 3),
                            Map.entry("basePrice", 350.0),
                            Map.entry("finalPrice", 350.0),
                            Map.entry("rating", 4.6),
                            Map.entry("inclusions", List.of("Boutique hotels", "Train ride", "Tea factory tour", "Hiking guide", "Breakfast")),
                            Map.entry("isFeatured", false),
                            Map.entry("imageUrl", "https://images.unsplash.com/photo-1587974928442-77dc3e0dba72")
                    )
            );

            @Override
            public Object searchPackages(String destination, Integer durationDays,
                                         Double minBudget, Double maxBudget, String query, int page, int size) {
                var filtered = packages.stream()
                        .filter(p -> destination == null || p.get("name").toString().toLowerCase().contains(destination.toLowerCase())
                                || p.get("description").toString().toLowerCase().contains(destination.toLowerCase())
                                || ((List<?>) p.get("destinations")).stream()
                                        .anyMatch(d -> d.toString().toLowerCase().contains(destination.toLowerCase())))
                        .filter(p -> durationDays == null || (int) p.get("durationDays") <= durationDays)
                        .filter(p -> minBudget == null || (double) p.get("basePrice") >= minBudget)
                        .filter(p -> maxBudget == null || (double) p.get("basePrice") <= maxBudget)
                        .filter(p -> query == null || p.get("name").toString().toLowerCase().contains(query.toLowerCase())
                                || p.get("description").toString().toLowerCase().contains(query.toLowerCase()))
                        .toList();
                Map<String, Object> result = new LinkedHashMap<>();
                result.put("content", filtered);
                result.put("page", page);
                result.put("size", size);
                result.put("totalElements", filtered.size());
                return result;
            }

            @Override
            public Object getPackageById(String id) {
                return packages.stream()
                        .filter(p -> String.valueOf(p.get("id")).equals(id))
                        .findFirst()
                        .map(p -> Map.<String, Object>of("data", p))
                        .orElse(Map.of("data", Map.of()));
            }
        };
    }

    // ==================== EVENT SERVICE ====================
    @Bean
    public EventServiceClient mockEventServiceClient() {
        final List<Map<String, Object>> events = List.of(
                Map.ofEntries(
                        Map.entry("id", 1L),
                        Map.entry("title", "Kandy Esala Perahera"),
                        Map.entry("description", "Sri Lanka's most spectacular Buddhist festival featuring fire dancers, whip crackers, and magnificently decorated elephants."),
                        Map.entry("category", "CULTURAL"),
                        Map.entry("location", "Kandy"),
                        Map.entry("startDate", "2026-08-01"),
                        Map.entry("endDate", "2026-08-10"),
                        Map.entry("price", 0.0),
                        Map.entry("availableTickets", 500),
                        Map.entry("imageUrl", "https://images.unsplash.com/photo-1531259683007-016a7b628fc3"),
                        Map.entry("authenticCultural", true)
                ),
                Map.ofEntries(
                        Map.entry("id", 2L),
                        Map.entry("title", "Colombo Food Festival"),
                        Map.entry("description", "Annual celebration of Sri Lankan cuisine featuring over 100 food stalls, live cooking shows, and award-winning local chefs."),
                        Map.entry("category", "FOOD"),
                        Map.entry("location", "Colombo"),
                        Map.entry("startDate", "2026-03-15"),
                        Map.entry("endDate", "2026-03-17"),
                        Map.entry("price", 5.0),
                        Map.entry("availableTickets", 200),
                        Map.entry("imageUrl", "https://images.unsplash.com/photo-1555126634-323283e090fa"),
                        Map.entry("authenticCultural", false)
                ),
                Map.ofEntries(
                        Map.entry("id", 3L),
                        Map.entry("title", "Galle Literary Festival"),
                        Map.entry("description", "International literary festival in the historic Galle Fort featuring authors, panel discussions, and cultural performances."),
                        Map.entry("category", "CULTURAL"),
                        Map.entry("location", "Galle"),
                        Map.entry("startDate", "2026-01-20"),
                        Map.entry("endDate", "2026-01-24"),
                        Map.entry("price", 10.0),
                        Map.entry("availableTickets", 150),
                        Map.entry("imageUrl", "https://images.unsplash.com/photo-1544161515-4ab6ce6db874"),
                        Map.entry("authenticCultural", true)
                ),
                Map.ofEntries(
                        Map.entry("id", 4L),
                        Map.entry("title", "Yala Wildlife Photography Tour"),
                        Map.entry("description", "Guided wildlife photography safari in Yala National Park with expert naturalist, targeting leopards and elephants."),
                        Map.entry("category", "ADVENTURE"),
                        Map.entry("location", "Yala"),
                        Map.entry("startDate", "2026-03-10"),
                        Map.entry("endDate", "2026-03-10"),
                        Map.entry("price", 75.0),
                        Map.entry("availableTickets", 12),
                        Map.entry("imageUrl", "https://images.unsplash.com/photo-1516426122078-c23e76319801"),
                        Map.entry("authenticCultural", false)
                ),
                Map.ofEntries(
                        Map.entry("id", 5L),
                        Map.entry("title", "Ella Hiking Adventure"),
                        Map.entry("description", "Guided sunrise hike to Little Adam's Peak followed by Nine Arches Bridge walk with local breakfast."),
                        Map.entry("category", "ADVENTURE"),
                        Map.entry("location", "Ella"),
                        Map.entry("startDate", "2026-03-05"),
                        Map.entry("endDate", "2026-12-31"),
                        Map.entry("price", 25.0),
                        Map.entry("availableTickets", 20),
                        Map.entry("imageUrl", "https://images.unsplash.com/photo-1587974928442-77dc3e0dba72"),
                        Map.entry("authenticCultural", false)
                )
        );

        return new EventServiceClient() {
            @Override
            public Object browseEvents(String category, String location, String dateFrom, String dateTo,
                                       Double minPrice, Double maxPrice, int page, int size) {
                var filtered = events.stream()
                        .filter(e -> location == null || e.get("location").toString().toLowerCase().contains(location.toLowerCase()))
                        .filter(e -> category == null || e.get("category").toString().equalsIgnoreCase(category))
                        .filter(e -> minPrice == null || (double) e.get("price") >= minPrice)
                        .filter(e -> maxPrice == null || (double) e.get("price") <= maxPrice)
                        .toList();
                Map<String, Object> result = new LinkedHashMap<>();
                result.put("content", filtered);
                result.put("page", page);
                result.put("size", size);
                result.put("totalElements", filtered.size());
                return result;
            }

            @Override
            public Object getEventById(Long id) {
                return events.stream()
                        .filter(e -> e.get("id").equals(id))
                        .findFirst()
                        .map(e -> Map.<String, Object>of("data", e))
                        .orElse(Map.of("data", Map.of()));
            }
        };
    }

    // ==================== ECOMMERCE SERVICE ====================
    @Bean
    public EcommerceServiceClient mockEcommerceServiceClient() {
        final List<Map<String, Object>> products = List.of(
                Map.ofEntries(
                        Map.entry("id", 1L),
                        Map.entry("name", "Ceylon Sapphire Tea Collection"),
                        Map.entry("description", "Premium collection of 6 single-estate Ceylon teas from Nuwara Eliya, Dimbula, and Uva regions."),
                        Map.entry("category", "FOOD"),
                        Map.entry("price", 18.0),
                        Map.entry("stock", 50),
                        Map.entry("imageUrl", "https://images.unsplash.com/photo-1564890369478-c89ca3d9cff0")
                ),
                Map.ofEntries(
                        Map.entry("id", 2L),
                        Map.entry("name", "Sri Lanka Batik Sarong"),
                        Map.entry("description", "Handcrafted traditional batik sarong with authentic Sri Lankan motifs, made by local artisans in Kandy."),
                        Map.entry("category", "CLOTHING"),
                        Map.entry("price", 22.0),
                        Map.entry("stock", 30),
                        Map.entry("imageUrl", "https://images.unsplash.com/photo-1589652717521-10c0d092dea9")
                ),
                Map.ofEntries(
                        Map.entry("id", 3L),
                        Map.entry("name", "Handloom Elephant Tote Bag"),
                        Map.entry("description", "Eco-friendly tote bag woven from natural jute with traditional elephant design. Perfect for shopping and beach."),
                        Map.entry("category", "SOUVENIR"),
                        Map.entry("price", 12.0),
                        Map.entry("stock", 75),
                        Map.entry("imageUrl", "https://images.unsplash.com/photo-1553062407-98eeb64c6a62")
                ),
                Map.ofEntries(
                        Map.entry("id", 4L),
                        Map.entry("name", "Travel First Aid Kit"),
                        Map.entry("description", "Compact travel first aid kit tailored for Sri Lanka outdoor activities, includes insect repellent and sun protection."),
                        Map.entry("category", "GEAR"),
                        Map.entry("price", 28.0),
                        Map.entry("stock", 40),
                        Map.entry("imageUrl", "https://images.unsplash.com/photo-1603398938378-e54eab446dde")
                ),
                Map.ofEntries(
                        Map.entry("id", 5L),
                        Map.entry("name", "Ceylon Cinnamon & Spice Gift Box"),
                        Map.entry("description", "Authentic Ceylon cinnamon, cardamom, cloves, and pepper — hand-packed in a keepsake wooden box from Galle."),
                        Map.entry("category", "FOOD"),
                        Map.entry("price", 15.0),
                        Map.entry("stock", 60),
                        Map.entry("imageUrl", "https://images.unsplash.com/photo-1599058917212-d750089bc07e")
                )
        );

        return new EcommerceServiceClient() {
            @Override
            public Object getProducts(String category, BigDecimal minPrice, BigDecimal maxPrice, String eventId) {
                return products.stream()
                        .filter(p -> category == null || p.get("category").toString().equalsIgnoreCase(category))
                        .filter(p -> minPrice == null || BigDecimal.valueOf((double) p.get("price")).compareTo(minPrice) >= 0)
                        .filter(p -> maxPrice == null || BigDecimal.valueOf((double) p.get("price")).compareTo(maxPrice) <= 0)
                        .toList();
            }

            @Override
            public Object getProductById(Long id) {
                return products.stream()
                        .filter(p -> p.get("id").equals(id))
                        .findFirst()
                        .map(p -> Map.<String, Object>of("data", p))
                        .orElse(Map.of("data", Map.of()));
            }
        };
    }
}
