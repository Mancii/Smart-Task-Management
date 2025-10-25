package com.task.service;

import com.task.entity.User;

import java.io.IOException;

public interface EmailServiceInterface {
    void sendVerificationEmail(User user, String token) throws IOException;
    void sendPasswordResetEmail(User user, String resetUrl, String token) throws IOException;
}
