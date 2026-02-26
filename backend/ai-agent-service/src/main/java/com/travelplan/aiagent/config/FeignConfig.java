package com.travelplan.aiagent.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Enables Feign clients only in non-dev profiles.
 * In dev profile, MockFeignClients provides mock implementations instead.
 */
@Configuration
@Profile("!dev")
@EnableFeignClients(basePackages = "com.travelplan.aiagent.client")
public class FeignConfig {
}
