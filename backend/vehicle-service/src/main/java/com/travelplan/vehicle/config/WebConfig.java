package com.travelplan.vehicle.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Map "http://localhost:8085/uploads/filename.jpg" -> to the local E: drive folder
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:E:/travel-plan-platform/travel-plan-uploads/");
    }
    
    // NOTE: addCorsMappings is intentionally removed here because 
    // it is already handled safely inside your SecurityConfig.java!
}