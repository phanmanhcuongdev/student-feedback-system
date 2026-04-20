package com.ttcs.backend.adapter.in.web.dto;

public record QuestionBankRequest(
        String content,
        String type,
        String category
) {
}
