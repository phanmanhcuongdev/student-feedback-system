package com.ttcs.backend.application.port.in.admin;

public interface SetQuestionBankEntryActiveUseCase {
    QuestionBankEntryResult setActive(Integer id, boolean active);
}
