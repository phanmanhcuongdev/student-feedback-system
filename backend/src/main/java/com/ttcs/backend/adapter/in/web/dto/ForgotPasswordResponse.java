package com.ttcs.backend.adapter.in.web.dto;

public record ForgotPasswordResponse(
        boolean success,
        String code,
        String message
) {
}
