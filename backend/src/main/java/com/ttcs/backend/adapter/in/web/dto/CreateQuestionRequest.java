package com.ttcs.backend.adapter.in.web.dto;

public record CreateQuestionRequest(
        String content,
        String type
) {
}
