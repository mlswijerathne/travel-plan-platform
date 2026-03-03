package com.travelplan.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Shared OpenAPI / Swagger configuration for all Travel Plan microservices.
 *
 * <p>Each service customises the title, description, and version via
 * {@code application.yml} properties:
 *
 * <pre>
 * openapi:
 *   info:
 *     title: "Hotel Service API"
 *     description: "Manages hotel listings, availability, and ratings"
 *     version: "1.0.0"
 * </pre>
 *
 * <p>A global JWT Bearer security scheme is registered so the Swagger UI
 * "Authorize" button accepts a Supabase JWT for testing protected endpoints.
 *
 * <p>{@code @ConditionalOnMissingBean} allows a service to override this bean
 * entirely by declaring its own {@link OpenAPI} bean.
 */
@Configuration
@ConditionalOnClass(OpenAPI.class)
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "bearerAuth";

    @Value("${openapi.info.title:Travel Plan API}")
    private String title;

    @Value("${openapi.info.description:Travel Plan Platform microservice API}")
    private String description;

    @Value("${openapi.info.version:1.0.0}")
    private String version;

    @Bean
    @ConditionalOnMissingBean(OpenAPI.class)
    public OpenAPI serviceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title(title)
                        .description(description)
                        .version(version)
                        .contact(new Contact()
                                .name("Travel Plan Platform")
                                .email("admin@travelplan.com")))
                .components(new Components()
                        .addSecuritySchemes(BEARER_SCHEME,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Paste your Supabase JWT (obtain via POST /auth/v1/token)")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME));
    }
}
