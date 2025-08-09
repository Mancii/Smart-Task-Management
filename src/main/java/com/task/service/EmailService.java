package com.task.service;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.task.config.VaultConfig;
import com.task.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Year;
import java.util.Map;

@Service
@Slf4j
@EnableConfigurationProperties(VaultConfig.class)
@ConditionalOnProperty(name = "app.email.mock", havingValue = "false", matchIfMissing = false)
public class EmailService implements EmailServiceInterface {
    private final SendGrid sendGrid;
    private final TemplateService templateService;
    private final String fromEmail;
    private final String appBaseUrl;

    public EmailService(@Value("${app.email.sender}") String sender,
                        @Value("${app.email.apiKey}") String apiKey,
                       @Value("${app.base-url}") String appBaseUrl,
                       TemplateService templateService) {
        this.sendGrid = new SendGrid(apiKey);
        this.fromEmail = sender;
        this.appBaseUrl = appBaseUrl;
        this.templateService = templateService;
    }

    public void sendVerificationEmail(User user, String token) throws IOException {
        String verificationLink = String.format("%s/api/auth/verify-email?token=%s",
                appBaseUrl, token);
        String subject = "Verify Your Email Address";

        // Prepare template variables
        Map<String, Object> variables = Map.of(
                "username", user.getUsername(),
                "verificationLink", verificationLink,
                "currentYear", Year.now().getValue()
        );

        // Process template
        String htmlContent = templateService.processTemplate("verification-email.html", variables);

        Email from = new Email(fromEmail, "Task Management");
        Email to = new Email(user.getEmail(), user.getUsername());
        Content content = new Content("text/html", htmlContent);

        Mail mail = new Mail(from, subject, to, content);
        sendEmail(mail);
    }

    private void sendEmail(Mail mail) throws IOException {
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sendGrid.api(request);

            if (response.getStatusCode() >= 400) {
                log.error("Failed to send email. Status: {}, Body: {}",
                        response.getStatusCode(), response.getBody());
                throw new IOException("Failed to send email: " + response.getBody());
            }

            log.info("Email sent to {}",
                    mail.personalization.getFirst().getTos().getFirst().getEmail());

        } catch (IOException ex) {
            log.error("Error sending email", ex);
            throw ex;
        }
    }

    public void sendPasswordResetEmail(User user, String resetUrl, String token) {
        // Implementation for password reset email (can be implemented later)
    }

}
