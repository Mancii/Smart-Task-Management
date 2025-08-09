package com.task.service;

import com.task.config.VaultConfig;
import com.task.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.time.Year;
import java.util.Map;

@Service
@Slf4j
@ConditionalOnProperty(name = "app.email.mock", havingValue = "true")
public class MockEmailService implements EmailServiceInterface {
    private final TemplateService templateService;
    private final String fromEmail;
    private final String appBaseUrl;

    public MockEmailService(VaultConfig vaultConfig,
                            @Value("${app.base-url}") String appBaseUrl,
                            TemplateService templateService) {
        this.fromEmail = vaultConfig.getEmail().getSender();
        this.appBaseUrl = appBaseUrl;
        this.templateService = templateService;
    }

    @Override
    public void sendVerificationEmail(User user, String token) throws IOException {
        String verificationLink = String.format("%s/api/auth/verify-email?token=%s",
                appBaseUrl, token);

        // Prepare template variables
        Map<String, Object> variables = Map.of(
                "username", user.getUsername(),
                "verificationLink", verificationLink,
                "currentYear", Year.now().getValue()
        );

        // Process template
        String htmlContent = templateService.processTemplate("verification-email.html", variables);

        String emailContent = String.format(
                "\n=== MOCK EMAIL SENT ===\n" +
                "From: %s\n" +
                "To: %s <%s>\n" +
                "Subject: Verify Your Email Address\n\n" +
                "%s\n" +
                "=====================\n",
                fromEmail,
                user.getUsername(),
                user.getEmail(),
                htmlContent
        );

        log.info(emailContent);
    }

    @Override
    public void sendPasswordResetEmail(User user, String resetUrl, String token) {
        log.info("\n=== MOCK PASSWORD RESET EMAIL SENT ===\n" +
                "To: {} <{}>\n" +
                "Reset URL: {}\n" +
                "Token: {}\n" +
                "==============================\n",
                user.getUsername(),
                user.getEmail(),
                resetUrl,
                token
        );
    }
}