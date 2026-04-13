package com.ttcs.backend.adapter.in.web.dto;

public record RespondToFeedbackResponse(
        boolean success,
        String code,
        String message
) {
}
