package com.ttcs.backend.config;

import org.springframework.security.crypto.password.PasswordEncoder;

// DEV ONLY: encoder plain text để tương thích dữ liệu cũ tạm thời.
public class PlainTextPasswordEncoder implements PasswordEncoder {

    @Override
    public String encode(CharSequence rawPassword) {
        return rawPassword == null ? null : rawPassword.toString();
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (rawPassword == null && encodedPassword == null) return true;
        if (rawPassword == null || encodedPassword == null) return false;
        return rawPassword.toString().equals(encodedPassword);
    }
}
