package com.travelplan.aiagent.config;

import com.netflix.discovery.shared.transport.jersey.TransportClientFactories;
import com.netflix.discovery.shared.transport.jersey3.Jersey3TransportClientFactories;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Forces Jersey3 Eureka transport when both spring-boot-starter-web and
 * spring-boot-starter-webflux are on the classpath.
 * MutableDiscoveryClientOptionalArgs was removed in Spring Cloud 2025.x;
 * Spring Cloud auto-configuration now handles the optional-args bean itself.
 */
@Configuration
public class EurekaTransportConfig {

    @Bean
    @ConditionalOnMissingBean(TransportClientFactories.class)
    public Jersey3TransportClientFactories jersey3TransportClientFactories() {
        return new Jersey3TransportClientFactories();
    }
}
