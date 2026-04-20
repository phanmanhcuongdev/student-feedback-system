package com.ttcs.backend.application.port.out.admin;

public record QuestionBankSearchQuery(
        String keyword,
        String type,
        String category,
        Boolean active,
        int page,
        int size
) {
}
