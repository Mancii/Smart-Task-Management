package com.task.service;

import com.task.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@ConditionalOnProperty(name = "app.email.mock", havingValue = "false", matchIfMissing = false)
public class EmailService {

    private final JavaMailSender mailSender;
    private final String appBaseUrl;
    private final String fromEmail;

    public EmailService(JavaMailSender mailSender,
                        @Value("${app.base-url}") String appBaseUrl,
                        @Value("${app.email.sender}") String fromEmail) {
        this.mailSender = mailSender;
        this.appBaseUrl = appBaseUrl;
        this.fromEmail = fromEmail;
    }

    public void sendVerificationEmail(User user, String token) {
        String subject = "Email Verification";
        String verificationLink = buildVerificationLink(token);

        String message = String.format(
            "Dear %s,%n%n" +
            "Thank you for registering. Please click the link below to verify your email address:%n%n" +
            "%s%n%n" +
            "This link will expire in 24 hours.%n%n" +
            "Best regards,%nThe Task Management Team",
            user.getUsername(),
            verificationLink
        );

        sendEmail(user.getEmail(), subject, message);
    }

    private String buildVerificationLink(String token) {
        return UriComponentsBuilder.newInstance()
                .scheme("https")
                .host(appBaseUrl.replace("https://", "").replace("http://", ""))
                .path("/api/auth/verify-email")
                .queryParam("token", token)
                .toUriString();
    }

    public void sendPasswordResetEmail(User user, String resetUrl, String token) {
        // Implementation for password reset email (can be implemented later)
    }

    private void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        
        // In production, you might want to use async email sending
        new Thread(() -> mailSender.send(message)).start();
    }
}
