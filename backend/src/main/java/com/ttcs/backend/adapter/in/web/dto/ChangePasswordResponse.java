package com.ttcs.backend.adapter.in.web.dto;

public record ChangePasswordResponse(
        boolean success,
        String code,
        String message
) {
}
