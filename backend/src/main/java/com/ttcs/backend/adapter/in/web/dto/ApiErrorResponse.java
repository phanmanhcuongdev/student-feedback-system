package com.ttcs.backend.adapter.in.web.dto;

public record ApiErrorResponse(
        String code,
        String message
) {
}
