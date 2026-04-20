package com.ttcs.backend.adapter.in.web.dto;

public record NotificationActionResponse(
        boolean success,
        String code,
        String message
) {
}
