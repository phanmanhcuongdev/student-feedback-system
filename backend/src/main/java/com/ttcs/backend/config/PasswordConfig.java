package com.ttcs.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordConfig {

    @Bean
    public PasswordEncoder passwordEncoder(
            @Value("${app.security.password-plain-text:true}") boolean plainTextMode
    ) {
        if (plainTextMode) {
            // DEV ONLY: lưu plain text
            return new PlainTextPasswordEncoder();
        }
        return new BCryptPasswordEncoder();
    }
}
