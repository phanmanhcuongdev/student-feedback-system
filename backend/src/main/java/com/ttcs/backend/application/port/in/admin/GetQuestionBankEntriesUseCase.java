package com.ttcs.backend.application.port.in.admin;

public interface GetQuestionBankEntriesUseCase {
    QuestionBankEntryPageResult list(GetQuestionBankEntriesQuery query);
}
