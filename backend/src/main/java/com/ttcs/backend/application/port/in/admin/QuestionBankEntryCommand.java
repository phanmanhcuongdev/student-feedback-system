package com.ttcs.backend.application.port.in.admin;

public record QuestionBankEntryCommand(
        String content,
        String type,
        String category
) {
}
