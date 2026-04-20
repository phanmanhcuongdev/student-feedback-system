package com.ttcs.backend.application.port.in.command;

import com.ttcs.backend.application.domain.model.QuestionType;

public record CreateQuestionCommand(
        String content,
        QuestionType type,
        Integer questionBankEntryId
) {
    public CreateQuestionCommand(String content, QuestionType type) {
        this(content, type, null);
    }
}
