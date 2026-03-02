package com.travelplan.review.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger configuration for the Review Service.
 *
 * <p>Exposes interactive API documentation at
 * {@code http://localhost:8088/swagger-ui.html} when the service is running.
 *
 * <p>A global Bearer-token security scheme is registered so the Swagger UI
 * "Authorize" button accepts a Supabase JWT for testing protected endpoints.
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "bearerAuth";

    /**
     * Defines the top-level OpenAPI metadata and the JWT security scheme.
     *
     * @return fully configured {@link OpenAPI} bean
     */
    @Bean
    public OpenAPI reviewServiceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Review Service API")
                        .description("""
                                Manages tourist reviews for hotels, tour guides, and vehicles.
                                
                                **Features:**
                                - Submit 1–5 star reviews with optional text, title, and photos
                                - View reviews for any provider entity
                                - Aggregate rating summaries with distribution histograms
                                - Providers can publicly respond to reviews
                                - Pending review queue populated from trip-completion events
                                - Rating update events published to SQS on every write
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Travel Plan Platform Team")))
                // Register the Bearer token security scheme
                .components(new Components()
                        .addSecuritySchemes(BEARER_SCHEME,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Paste your Supabase JWT here")))
                // Apply the security scheme globally to all endpoints
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME));
    }
}
