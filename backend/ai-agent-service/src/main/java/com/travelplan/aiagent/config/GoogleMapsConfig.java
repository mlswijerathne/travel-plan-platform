package com.travelplan.aiagent.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Configuration
public class GoogleMapsConfig {

    @Getter
    @Value("${google.maps.api-key:}")
    private String apiKey;

    @Getter
    @Value("${google.maps.use-dummy:false}")
    private boolean useDummy;

    @Value("${google.maps.base-url:https://maps.googleapis.com/maps/api}")
    private String baseUrl;

    @Bean
    public WebClient googleMapsWebClient() {
        log.info("Initializing Google Maps WebClient with base URL: {}", baseUrl);

        if (apiKey == null || apiKey.isBlank()) {
            log.warn("GOOGLE_MAPS_API_KEY is not configured. Google Maps features will return fallback data.");
        }

        return WebClient.builder()
                .baseUrl(baseUrl)
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(512 * 1024))
                .build();
    }
}
