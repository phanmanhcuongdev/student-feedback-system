package com.ttcs.backend.application.port.out.auth;

public interface JwtTokenPort {
    String generateAccessToken(Integer userId, String email, String role);

    boolean isTokenValid(String token);

    String extractEmail(String token);

    String extractRole(String token);
}
