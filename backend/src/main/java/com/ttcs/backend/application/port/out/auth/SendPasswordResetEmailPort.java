package com.ttcs.backend.application.port.out.auth;

public interface SendPasswordResetEmailPort {
    void sendPasswordResetEmail(String toEmail, String resetUrl);
}
