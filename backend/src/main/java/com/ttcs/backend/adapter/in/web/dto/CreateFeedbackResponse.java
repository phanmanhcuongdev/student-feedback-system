package com.ttcs.backend.adapter.in.web.dto;

public record CreateFeedbackResponse(
        boolean success,
        String code,
        String message
) {
}
