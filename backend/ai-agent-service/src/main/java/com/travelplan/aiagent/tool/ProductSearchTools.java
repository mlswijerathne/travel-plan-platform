package com.travelplan.aiagent.tool;

import com.google.adk.tools.Annotations.Schema;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class ProductSearchTools {

    @Schema(description = "Search for travel products and souvenirs registered on the platform. Returns platform-listed products including travel gear, local crafts, and trip essentials.")
    public static Map<String, Object> searchProducts(
            @Schema(description = "Product category, e.g. 'SOUVENIR', 'GEAR', 'CLOTHING', 'FOOD', 'HANDICRAFT'. Pass empty string to search all.") String category,
            @Schema(description = "Minimum price in USD, pass 0 to skip") double minPrice,
            @Schema(description = "Maximum price in USD, pass 0 to skip") double maxPrice) {
        try {
            log.info("Searching products - category: {}, minPrice: {}, maxPrice: {}", category, minPrice, maxPrice);

            String categoryParam = (category != null && !category.isBlank()) ? category : null;
            BigDecimal minParam = minPrice > 0 ? BigDecimal.valueOf(minPrice) : null;
            BigDecimal maxParam = maxPrice > 0 ? BigDecimal.valueOf(maxPrice) : null;

            Object response = ToolRegistry.getInstance().getEcommerceServiceClient()
                    .getProducts(categoryParam, minParam, maxParam, null);

            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("source", "Platform Partner");
            result.put("data", response);
            return result;
        } catch (Exception e) {
            log.error("Error searching products: {} - {}", e.getClass().getSimpleName(), e.getMessage(), e);
            Map<String, Object> result = new HashMap<>();
            result.put("status", "error");
            result.put("message", "Product service is currently unavailable. Error: " + e.getMessage());
            return result;
        }
    }

    @Schema(description = "Get detailed information about a specific platform product by its ID.")
    public static Map<String, Object> getProductDetails(
            @Schema(description = "The unique numeric ID of the product to retrieve details for") long productId) {
        try {
            log.info("Getting product details for ID: {}", productId);

            Object response = ToolRegistry.getInstance().getEcommerceServiceClient()
                    .getProductById(productId);

            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("source", "Platform Partner");
            result.put("data", response);
            return result;
        } catch (Exception e) {
            log.error("Error getting product details: {}", e.getMessage());
            Map<String, Object> result = new HashMap<>();
            result.put("status", "error");
            result.put("message", "Could not retrieve product details. Product ID: " + productId);
            return result;
        }
    }
}
