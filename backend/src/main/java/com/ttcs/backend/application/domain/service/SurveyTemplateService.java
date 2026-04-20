package com.ttcs.backend.application.domain.service;

import com.ttcs.backend.application.domain.model.QuestionType;
import com.ttcs.backend.application.domain.model.SurveyTemplate;
import com.ttcs.backend.application.domain.model.SurveyTemplateQuestion;
import com.ttcs.backend.application.domain.model.SurveyRecipientScope;
import com.ttcs.backend.application.port.in.admin.ApplySurveyTemplateUseCase;
import com.ttcs.backend.application.port.in.admin.CreateSurveyTemplateUseCase;
import com.ttcs.backend.application.port.in.admin.GetSurveyTemplateUseCase;
import com.ttcs.backend.application.port.in.admin.GetSurveyTemplatesQuery;
import com.ttcs.backend.application.port.in.admin.GetSurveyTemplatesUseCase;
import com.ttcs.backend.application.port.in.admin.SetSurveyTemplateActiveUseCase;
import com.ttcs.backend.application.port.in.admin.SurveyTemplateCommand;
import com.ttcs.backend.application.port.in.admin.SurveyTemplatePageResult;
import com.ttcs.backend.application.port.in.admin.SurveyTemplateQuestionCommand;
import com.ttcs.backend.application.port.in.admin.SurveyTemplateQuestionResult;
import com.ttcs.backend.application.port.in.admin.SurveyTemplateResult;
import com.ttcs.backend.application.port.in.admin.UpdateSurveyTemplateUseCase;
import com.ttcs.backend.application.port.out.admin.ManageQuestionBankPort;
import com.ttcs.backend.application.port.out.admin.ManageSurveyTemplatePort;
import com.ttcs.backend.application.port.out.admin.SurveyTemplateSearchQuery;
import com.ttcs.backend.common.UseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@UseCase
@RequiredArgsConstructor
public class SurveyTemplateService implements
        GetSurveyTemplatesUseCase,
        GetSurveyTemplateUseCase,
        ApplySurveyTemplateUseCase,
        CreateSurveyTemplateUseCase,
        UpdateSurveyTemplateUseCase,
        SetSurveyTemplateActiveUseCase {

    private final ManageSurveyTemplatePort manageSurveyTemplatePort;
    private final ManageQuestionBankPort manageQuestionBankPort;

    @Override
    @Transactional(readOnly = true)
    public SurveyTemplatePageResult list(GetSurveyTemplatesQuery query) {
        var page = manageSurveyTemplatePort.loadPage(new SurveyTemplateSearchQuery(
                query == null ? null : query.keyword(),
                query == null ? null : query.active(),
                query == null ? 0 : query.page(),
                query == null ? 20 : query.size()
        ));
        return new SurveyTemplatePageResult(
                page.items().stream().map(this::toResult).toList(),
                page.page(),
                page.size(),
                page.totalElements(),
                page.totalPages()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public SurveyTemplateResult get(Integer id) {
        return manageSurveyTemplatePort.loadById(id)
                .map(this::toResult)
                .orElseThrow(() -> new IllegalArgumentException("SURVEY_TEMPLATE_NOT_FOUND"));
    }

    @Override
    @Transactional(readOnly = true)
    public SurveyTemplateResult apply(Integer id) {
        SurveyTemplate template = manageSurveyTemplatePort.loadById(id)
                .orElseThrow(() -> new IllegalArgumentException("SURVEY_TEMPLATE_NOT_FOUND"));
        if (!template.active()) {
            throw new IllegalArgumentException("SURVEY_TEMPLATE_ARCHIVED");
        }
        return toResult(template);
    }

    @Override
    @Transactional
    public SurveyTemplateResult create(SurveyTemplateCommand command) {
        validate(command);
        SurveyTemplate template = toTemplate(null, command, true, LocalDateTime.now(), null);
        return toResult(manageSurveyTemplatePort.save(template));
    }

    @Override
    @Transactional
    public SurveyTemplateResult update(Integer id, SurveyTemplateCommand command) {
        validate(command);
        SurveyTemplate existing = manageSurveyTemplatePort.loadById(id)
                .orElseThrow(() -> new IllegalArgumentException("SURVEY_TEMPLATE_NOT_FOUND"));
        SurveyTemplate template = toTemplate(existing, command, existing.active(), existing.createdAt(), LocalDateTime.now());
        return toResult(manageSurveyTemplatePort.save(template));
    }

    @Override
    @Transactional
    public SurveyTemplateResult setActive(Integer id, boolean active) {
        SurveyTemplate existing = manageSurveyTemplatePort.loadById(id)
                .orElseThrow(() -> new IllegalArgumentException("SURVEY_TEMPLATE_NOT_FOUND"));
        return toResult(manageSurveyTemplatePort.save(new SurveyTemplate(
                existing.id(),
                existing.name(),
                existing.description(),
                existing.suggestedTitle(),
                existing.suggestedSurveyDescription(),
                existing.recipientScope(),
                existing.recipientDepartmentId(),
                active,
                existing.createdAt(),
                LocalDateTime.now(),
                existing.questions()
        )));
    }

    private void validate(SurveyTemplateCommand command) {
        if (command == null || command.name() == null || command.name().trim().isEmpty()) {
            throw new IllegalArgumentException("Template name is required.");
        }
        String scope = normalizeScope(command.recipientScope());
        if ("DEPARTMENT".equals(scope) && command.recipientDepartmentId() == null) {
            throw new IllegalArgumentException("Recipient department is required for department-scoped templates.");
        }
        if (command.questions() == null || command.questions().isEmpty()) {
            throw new IllegalArgumentException("At least one template question is required.");
        }
        for (SurveyTemplateQuestionCommand question : command.questions()) {
            if (question == null || question.content() == null || question.content().trim().isEmpty()) {
                throw new IllegalArgumentException("All template questions must have content.");
            }
            normalizeType(question.type());
            if (question.questionBankEntryId() != null && manageQuestionBankPort.loadById(question.questionBankEntryId()).isEmpty()) {
                throw new IllegalArgumentException("Question-bank entry was not found.");
            }
        }
    }

    private String normalizeScope(String scope) {
        if (scope == null || scope.isBlank()) {
            return SurveyRecipientScope.ALL_STUDENTS.name();
        }
        return SurveyRecipientScope.valueOf(scope.trim().toUpperCase()).name();
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

    private SurveyTemplate toTemplate(
            SurveyTemplate existing,
            SurveyTemplateCommand command,
            boolean active,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        String recipientScope = normalizeScope(command.recipientScope());
        return new SurveyTemplate(
                existing == null ? null : existing.id(),
                command.name().trim(),
                normalizeNullable(command.description()),
                normalizeNullable(command.suggestedTitle()),
                normalizeNullable(command.suggestedSurveyDescription()),
                recipientScope,
                "DEPARTMENT".equals(recipientScope) ? command.recipientDepartmentId() : null,
                active,
                createdAt,
                updatedAt,
                toQuestions(command.questions())
        );
    }

    private List<SurveyTemplateQuestion> toQuestions(List<SurveyTemplateQuestionCommand> questions) {
        java.util.concurrent.atomic.AtomicInteger index = new java.util.concurrent.atomic.AtomicInteger();
        return questions.stream()
                .map(question -> new SurveyTemplateQuestion(
                        null,
                        question.questionBankEntryId(),
                        question.content().trim(),
                        normalizeType(question.type()),
                        index.getAndIncrement()
                ))
                .toList();
    }

    private SurveyTemplateResult toResult(SurveyTemplate template) {
        return new SurveyTemplateResult(
                template.id(),
                template.name(),
                template.description(),
                template.suggestedTitle(),
                template.suggestedSurveyDescription(),
                template.recipientScope(),
                template.recipientDepartmentId(),
                template.active(),
                template.createdAt(),
                template.updatedAt(),
                template.questions().stream()
                        .map(this::toQuestionResult)
                        .toList()
        );
    }

    private SurveyTemplateQuestionResult toQuestionResult(SurveyTemplateQuestion question) {
        return new SurveyTemplateQuestionResult(
                question.id(),
                question.questionBankEntryId(),
                question.content(),
                question.type(),
                question.displayOrder()
        );
    }
}
