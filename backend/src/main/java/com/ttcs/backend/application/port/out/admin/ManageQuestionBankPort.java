package com.ttcs.backend.application.port.out.admin;

import com.ttcs.backend.application.domain.model.QuestionBankEntry;

import java.util.Optional;

public interface ManageQuestionBankPort {
    QuestionBankSearchPage loadPage(QuestionBankSearchQuery query);

    Optional<QuestionBankEntry> loadById(Integer id);

    QuestionBankEntry save(QuestionBankEntry entry);
}
