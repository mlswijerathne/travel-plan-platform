package com.travelplan.aiagent.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@FeignClient(name = "ecommerce-service", url = "${feign.ecommerce-service.url:}")
public interface EcommerceServiceClient {

    @GetMapping("/api/products")
    Object getProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String eventId);

    @GetMapping("/api/products/{id}")
    Object getProductById(@PathVariable Long id);
}
