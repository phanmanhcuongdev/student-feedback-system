package com.ttcs.backend.application.port.in.admin;

public record GetQuestionBankEntriesQuery(
        String keyword,
        String type,
        String category,
        Boolean active,
        int page,
        int size
) {
}
