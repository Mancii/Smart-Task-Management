package com.task.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "vault")
public class VaultConfig {
    private DatabaseConfig database;
    private EmailConfig email;
    private SecurityConfig security;

    @Getter
    @Setter
    public static class DatabaseConfig {
        private String username;
        private String password;
    }

    @Getter
    @Setter
    public static class EmailConfig {
        private String sender;
        private String apiKey;
    }

    @Getter
    @Setter
    public static class SecurityConfig {
        private String jwtKey;
    }
}
