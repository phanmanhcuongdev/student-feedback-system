package com.ttcs.backend.adapter.in.web.dto;

public record ResetPasswordResponse(
        boolean success,
        String code,
        String message
) {
}
