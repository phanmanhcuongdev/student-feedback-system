package com.ttcs.backend.adapter.in.web.dto;

public record UserManagementActionResponse(
        boolean success,
        String code,
        String message
) {
}
