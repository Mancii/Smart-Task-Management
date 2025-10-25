package com.task.service;

import java.io.IOException;

import com.task.entity.User;

public interface EmailServiceInterface {
void sendVerificationEmail(User user, String token) throws IOException;

void sendPasswordResetEmail(User user, String resetUrl, String token) throws IOException;
}
