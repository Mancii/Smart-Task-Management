package com.task.service;

import com.task.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@Slf4j
@ConditionalOnProperty(name = "app.email.mock", havingValue = "true")
public class MockEmailService {
    public void sendVerificationEmail(User user, String baseUrl, String token) {
        String verificationLink = UriComponentsBuilder.newInstance()
                .scheme("http")
                .host("localhost:8080") // or your dev server
                .path("/api/auth/verify-email")
                .queryParam("token", token)
                .build()
                .toUriString();

        String emailContent = String.format(
                "Mock Email - To: %s\n\n" +
                        "Dear %s,\n\n" +
                        "Please click the following link to verify your email:\n" +
                        "%s\n\n" +
                        "This link will expire in 24 hours.\n\n" +
                        "Best regards,\nThe Task Management Team",
                user.getEmail(),
                user.getUsername(),
                verificationLink
        );

        log.info("\n=== MOCK EMAIL SENT ===\n{}\n=====================", emailContent);
    }

}