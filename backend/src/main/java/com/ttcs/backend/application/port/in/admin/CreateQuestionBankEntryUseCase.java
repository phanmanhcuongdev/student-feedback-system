package com.ttcs.backend.application.port.in.admin;

public interface CreateQuestionBankEntryUseCase {
    QuestionBankEntryResult create(QuestionBankEntryCommand command);
}
