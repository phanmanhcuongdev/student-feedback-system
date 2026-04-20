package com.ttcs.backend.application.port.out.admin;

import com.ttcs.backend.application.domain.model.QuestionBankEntry;

import java.util.List;

public record QuestionBankSearchPage(
        List<QuestionBankEntry> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
