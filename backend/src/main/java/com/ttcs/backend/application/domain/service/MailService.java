package com.ttcs.backend.application.domain.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    public void sendVerifyEmail(String toEmail, String verifyUrl) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Xác nhận email đăng ký tài khoản");
        message.setText("Bấm vào link để xác nhận:\n" + verifyUrl);
        mailSender.send(message);
    }
}
