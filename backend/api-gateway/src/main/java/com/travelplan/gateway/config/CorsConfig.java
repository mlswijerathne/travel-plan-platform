package com.travelplan.gateway.config;

import org.springframework.context.annotation.Configuration;

/**
 * CORS is now handled via Spring Cloud Gateway's globalcors config in application.yml.
 * This class is kept empty to prevent common-lib's CorsConfig from activating.
 */
@Configuration
public class CorsConfig {
}
