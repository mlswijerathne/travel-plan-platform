package com.travelplan.aiagent.config;

import com.netflix.discovery.AbstractDiscoveryClientOptionalArgs;
import com.netflix.discovery.shared.transport.jersey.TransportClientFactories;
import com.netflix.discovery.shared.transport.jersey3.Jersey3TransportClientFactories;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.netflix.eureka.MutableDiscoveryClientOptionalArgs;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Forces servlet-mode Eureka transport when both spring-boot-starter-web and
 * spring-boot-starter-webflux are on the classpath. Without this, the reactive
 * auto-configuration creates ReactiveDiscoveryClientOptionalArgs which prevents
 * the Jersey3TransportClientFactories bean from being created.
 */
@Configuration
public class EurekaTransportConfig {

    @Bean
    @ConditionalOnMissingBean(AbstractDiscoveryClientOptionalArgs.class)
    public MutableDiscoveryClientOptionalArgs discoveryClientOptionalArgs() {
        return new MutableDiscoveryClientOptionalArgs();
    }

    @Bean
    @ConditionalOnMissingBean(TransportClientFactories.class)
    public Jersey3TransportClientFactories jersey3TransportClientFactories() {
        return new Jersey3TransportClientFactories();
    }
}
