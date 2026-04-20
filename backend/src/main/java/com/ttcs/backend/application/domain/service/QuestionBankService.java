package com.ttcs.backend.application.domain.service;

import com.ttcs.backend.application.domain.model.QuestionBankEntry;
import com.ttcs.backend.application.domain.model.QuestionType;
import com.ttcs.backend.application.port.in.admin.CreateQuestionBankEntryUseCase;
import com.ttcs.backend.application.port.in.admin.GetQuestionBankEntriesQuery;
import com.ttcs.backend.application.port.in.admin.GetQuestionBankEntriesUseCase;
import com.ttcs.backend.application.port.in.admin.QuestionBankEntryCommand;
import com.ttcs.backend.application.port.in.admin.QuestionBankEntryPageResult;
import com.ttcs.backend.application.port.in.admin.QuestionBankEntryResult;
import com.ttcs.backend.application.port.in.admin.SetQuestionBankEntryActiveUseCase;
import com.ttcs.backend.application.port.in.admin.UpdateQuestionBankEntryUseCase;
import com.ttcs.backend.application.port.out.admin.ManageQuestionBankPort;
import com.ttcs.backend.application.port.out.admin.QuestionBankSearchQuery;
import com.ttcs.backend.common.UseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@UseCase
@RequiredArgsConstructor
public class QuestionBankService implements
        GetQuestionBankEntriesUseCase,
        CreateQuestionBankEntryUseCase,
        UpdateQuestionBankEntryUseCase,
        SetQuestionBankEntryActiveUseCase {

    private final ManageQuestionBankPort manageQuestionBankPort;

    @Override
    @Transactional(readOnly = true)
    public QuestionBankEntryPageResult list(GetQuestionBankEntriesQuery query) {
        var page = manageQuestionBankPort.loadPage(new QuestionBankSearchQuery(
                query == null ? null : query.keyword(),
                query == null ? null : query.type(),
                query == null ? null : query.category(),
                query == null ? null : query.active(),
                query == null ? 0 : query.page(),
                query == null ? 20 : query.size()
        ));

        return new QuestionBankEntryPageResult(
                page.items().stream().map(this::toResult).toList(),
                page.page(),
                page.size(),
                page.totalElements(),
                page.totalPages()
        );
    }

    @Override
    @Transactional
    public QuestionBankEntryResult create(QuestionBankEntryCommand command) {
        validate(command);
        QuestionBankEntry entry = new QuestionBankEntry(
                null,
                command.content().trim(),
                normalizeType(command.type()),
                normalizeNullable(command.category()),
                true,
                LocalDateTime.now(),
                null
        );
        return toResult(manageQuestionBankPort.save(entry));
    }

    @Override
    @Transactional
    public QuestionBankEntryResult update(Integer id, QuestionBankEntryCommand command) {
        validate(command);
        QuestionBankEntry existing = manageQuestionBankPort.loadById(id)
                .orElseThrow(() -> new IllegalArgumentException("QUESTION_BANK_ENTRY_NOT_FOUND"));
        QuestionBankEntry updated = new QuestionBankEntry(
                existing.id(),
                command.content().trim(),
                normalizeType(command.type()),
                normalizeNullable(command.category()),
                existing.active(),
                existing.createdAt(),
                LocalDateTime.now()
        );
        return toResult(manageQuestionBankPort.save(updated));
    }

    @Override
    @Transactional
    public QuestionBankEntryResult setActive(Integer id, boolean active) {
        QuestionBankEntry existing = manageQuestionBankPort.loadById(id)
                .orElseThrow(() -> new IllegalArgumentException("QUESTION_BANK_ENTRY_NOT_FOUND"));
        QuestionBankEntry updated = new QuestionBankEntry(
                existing.id(),
                existing.content(),
                existing.type(),
                existing.category(),
                active,
                existing.createdAt(),
                LocalDateTime.now()
        );
        return toResult(manageQuestionBankPort.save(updated));
    }

    private void validate(QuestionBankEntryCommand command) {
        if (command == null || command.content() == null || command.content().trim().isEmpty()) {
            throw new IllegalArgumentException("Question content is required.");
        }
        normalizeType(command.type());
    }

    private String normalizeType(String type) {
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("Question type is required.");
        }
        return QuestionType.valueOf(type.trim().toUpperCase()).name();
    }

    private String normalizeNullable(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    private QuestionBankEntryResult toResult(QuestionBankEntry entry) {
        return new QuestionBankEntryResult(
                entry.id(),
                entry.content(),
                entry.type(),
                entry.category(),
                entry.active(),
                entry.createdAt(),
                entry.updatedAt()
        );
    }
}
