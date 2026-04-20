package com.ttcs.backend.application.port.in.admin;

import java.util.List;

public record QuestionBankEntryPageResult(
        List<QuestionBankEntryResult> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
