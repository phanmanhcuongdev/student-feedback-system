package com.ttcs.backend.application.port.out.auth;

public interface SendVerifyEmailPort {
    void sendVerifyEmail(String toEmail, String verifyUrl);
}
