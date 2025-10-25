package com.task.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Configuration for OpenAPI generation with custom examples and responses
 */
@Configuration
@Profile("openapi-gen")
public class OpenApiGeneratorConfig {

    @Bean
    public OpenApiCustomizer openApiCustomizer() {
        return openApi -> {
            // Add global response examples
            addGlobalResponseExamples(openApi);
            
            // Add custom tags
            addCustomTags(openApi);
            
            // Enhance security schemes
            enhanceSecuritySchemes(openApi);
        };
    }

    private void addGlobalResponseExamples(OpenAPI openApi) {
        // Success Response Example
        Example successExample = new Example()
                .summary("Successful Response")
                .description("Standard successful response format")
                .value("""
                    {
                        "success": true,
                        "message": "Operation completed successfully",
                        "data": {},
                        "timestamp": "2024-01-01T12:00:00Z",
                        "meta": {
                            "page": 0,
                            "size": 10,
                            "totalElements": 100,
                            "totalPages": 10
                        }
                    }
                    """);

        // Error Response Example
        Example errorExample = new Example()
                .summary("Error Response")
                .description("Standard error response format")
                .value("""
                    {
                        "success": false,
                        "message": "An error occurred",
                        "errorCode": "VALIDATION_ERROR",
                        "details": "Detailed error information",
                        "timestamp": "2024-01-01T12:00:00Z",
                        "path": "/api/endpoint"
                    }
                    """);

        // Add examples to components
        if (openApi.getComponents() != null) {
            openApi.getComponents()
                    .addExamples("SuccessResponse", successExample)
                    .addExamples("ErrorResponse", errorExample);
        }
    }

    private void addCustomTags(OpenAPI openApi) {
        // Tags are already defined in controllers, but we can add descriptions here
        if (openApi.getTags() != null) {
            openApi.getTags().forEach(tag -> {
                switch (tag.getName()) {
                    case "Authentication":
                        tag.setDescription("Endpoints for user authentication, registration, and token management");
                        break;
                    case "User Management":
                        tag.setDescription("Endpoints for managing users and user profiles");
                        break;
                    default:
                        break;
                }
            });
        }
    }

    private void enhanceSecuritySchemes(OpenAPI openApi) {
        // Security schemes are already defined in OpenApiConfig
        // This method can be used for additional enhancements if needed
    }

    /**
     * Creates a standard error response for OpenAPI documentation
     */
    public static ApiResponse createErrorResponse(String description, String example) {
        return new ApiResponse()
                .description(description)
                .content(new Content()
                        .addMediaType("application/json", new MediaType()
                                .example(example)));
    }

    /**
     * Creates a standard success response for OpenAPI documentation
     */
    public static ApiResponse createSuccessResponse(String description, String example) {
        return new ApiResponse()
                .description(description)
                .content(new Content()
                        .addMediaType("application/json", new MediaType()
                                .example(example)));
    }
}