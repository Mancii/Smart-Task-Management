package com.task.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${app.version:1.0.0}")
    private String appVersion;

    @Value("${app.title:Smart Task Management API}")
    private String appTitle;

    @Value("${app.description:A comprehensive task management system with user authentication and role-based access control}")
    private String appDescription;

    @Value("${server.url:http://localhost:8080}")
    private String serverUrl;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title(appTitle)
                        .version(appVersion)
                        .description(appDescription)
                        .contact(new Contact()
                                .name("Smart Task Management Team")
                                .email("support@smarttask.com")
                                .url("https://smarttask.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url(serverUrl)
                                .description("Development Server"),
                        new Server()
                                .url("https://api.smarttask.com")
                                .description("Production Server")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT token for authentication"))
                        .addSecuritySchemes("refreshToken", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("Refresh-Token")
                                .description("Refresh token for obtaining new access tokens")))
                .addSecurityItem(new SecurityRequirement()
                        .addList("bearerAuth"));
    }
}