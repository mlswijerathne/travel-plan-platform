package com.travelplan.aiagent.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Configuration
public class OpenStreetMapConfig {

    @Getter
    @Value("${osm.use-dummy:false}")
    private boolean useDummy;

    @Value("${osm.nominatim-base-url:https://nominatim.openstreetmap.org}")
    private String nominatimBaseUrl;

    @Value("${osm.osrm-base-url:https://router.project-osrm.org}")
    private String osrmBaseUrl;

    @Value("${osm.overpass-base-url:https://overpass-api.de/api}")
    private String overpassBaseUrl;

    @Value("${osm.user-agent:TravelPlanPlatform/1.0}")
    private String userAgent;

    @Bean
    public WebClient nominatimWebClient() {
        log.info("Initializing Nominatim WebClient with base URL: {}", nominatimBaseUrl);
        return WebClient.builder()
                .baseUrl(nominatimBaseUrl)
                .defaultHeader("User-Agent", userAgent)
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(512 * 1024))
                .build();
    }

    @Bean
    public WebClient osrmWebClient() {
        log.info("Initializing OSRM WebClient with base URL: {}", osrmBaseUrl);
        return WebClient.builder()
                .baseUrl(osrmBaseUrl)
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(512 * 1024))
                .build();
    }

    @Bean
    public WebClient overpassWebClient() {
        log.info("Initializing Overpass WebClient with base URL: {}", overpassBaseUrl);
        return WebClient.builder()
                .baseUrl(overpassBaseUrl)
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
                .build();
    }
}
