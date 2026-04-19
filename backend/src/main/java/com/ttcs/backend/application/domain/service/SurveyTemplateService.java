package com.ttcs.backend.application.domain.service;

import com.ttcs.backend.adapter.in.web.dto.SurveyTemplatePageResponse;
import com.ttcs.backend.adapter.in.web.dto.SurveyTemplateQuestionRequest;
import com.ttcs.backend.adapter.in.web.dto.SurveyTemplateQuestionResponse;
import com.ttcs.backend.adapter.in.web.dto.SurveyTemplateRequest;
import com.ttcs.backend.adapter.in.web.dto.SurveyTemplateResponse;
import com.ttcs.backend.adapter.out.persistence.questionbank.QuestionBankRepository;
import com.ttcs.backend.adapter.out.persistence.surveytemplate.SurveyTemplateEntity;
import com.ttcs.backend.adapter.out.persistence.surveytemplate.SurveyTemplateQuestionEntity;
import com.ttcs.backend.adapter.out.persistence.surveytemplate.SurveyTemplateRepository;
import com.ttcs.backend.application.domain.model.QuestionType;
import com.ttcs.backend.application.domain.model.SurveyRecipientScope;
import com.ttcs.backend.common.UseCase;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@UseCase
@RequiredArgsConstructor
public class SurveyTemplateService {

    private final SurveyTemplateRepository surveyTemplateRepository;
    private final QuestionBankRepository questionBankRepository;

    @Transactional(readOnly = true)
    public SurveyTemplatePageResponse list(String keyword, Boolean active, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        var result = surveyTemplateRepository.findAll(specification(keyword, active),
                PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "updatedAt").and(Sort.by(Sort.Direction.DESC, "createdAt"))));
        return new SurveyTemplatePageResponse(
                result.getContent().stream().map(this::toResponse).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    public SurveyTemplateResponse get(Integer id) {
        return surveyTemplateRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("SURVEY_TEMPLATE_NOT_FOUND"));
    }

    @Transactional(readOnly = true)
    public SurveyTemplateResponse apply(Integer id) {
        SurveyTemplateEntity entity = surveyTemplateRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("SURVEY_TEMPLATE_NOT_FOUND"));
        if (!entity.isActive()) {
            throw new IllegalArgumentException("SURVEY_TEMPLATE_ARCHIVED");
        }
        return toResponse(entity);
    }

    @Transactional
    public SurveyTemplateResponse create(SurveyTemplateRequest request) {
        validate(request);
        SurveyTemplateEntity entity = new SurveyTemplateEntity();
        entity.setCreatedAt(LocalDateTime.now());
        applyRequest(entity, request, true);
        return toResponse(surveyTemplateRepository.save(entity));
    }

    @Transactional
    public SurveyTemplateResponse update(Integer id, SurveyTemplateRequest request) {
        validate(request);
        SurveyTemplateEntity entity = surveyTemplateRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("SURVEY_TEMPLATE_NOT_FOUND"));
        entity.setUpdatedAt(LocalDateTime.now());
        applyRequest(entity, request, false);
        return toResponse(surveyTemplateRepository.save(entity));
    }

    @Transactional
    public SurveyTemplateResponse setActive(Integer id, boolean active) {
        SurveyTemplateEntity entity = surveyTemplateRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("SURVEY_TEMPLATE_NOT_FOUND"));
        entity.setActive(active);
        entity.setUpdatedAt(LocalDateTime.now());
        return toResponse(surveyTemplateRepository.save(entity));
    }

    private Specification<SurveyTemplateEntity> specification(String keyword, Boolean active) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (keyword != null && !keyword.isBlank()) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(builder.or(
                        builder.like(builder.lower(root.get("name")), pattern),
                        builder.like(builder.lower(root.get("description")), pattern),
                        builder.like(builder.lower(root.get("suggestedTitle")), pattern)
                ));
            }
            if (active != null) {
                predicates.add(builder.equal(root.get("active"), active));
            }
            return builder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private void applyRequest(SurveyTemplateEntity entity, SurveyTemplateRequest request, boolean activate) {
        entity.setName(request.name().trim());
        entity.setDescription(normalizeNullable(request.description()));
        entity.setSuggestedTitle(normalizeNullable(request.suggestedTitle()));
        entity.setSuggestedSurveyDescription(normalizeNullable(request.suggestedSurveyDescription()));
        entity.setRecipientScope(normalizeScope(request.recipientScope()));
        entity.setRecipientDepartmentId(entity.getRecipientScope().equals("DEPARTMENT") ? request.recipientDepartmentId() : null);
        if (activate) {
            entity.setActive(true);
        }
        entity.getQuestions().clear();

        int index = 0;
        for (SurveyTemplateQuestionRequest question : request.questions()) {
            SurveyTemplateQuestionEntity questionEntity = new SurveyTemplateQuestionEntity();
            questionEntity.setTemplate(entity);
            questionEntity.setQuestionBankEntryId(question.questionBankEntryId());
            questionEntity.setContent(question.content().trim());
            questionEntity.setType(normalizeType(question.type()));
            questionEntity.setDisplayOrder(index++);
            entity.getQuestions().add(questionEntity);
        }
    }

    private void validate(SurveyTemplateRequest request) {
        if (request == null || request.name() == null || request.name().trim().isEmpty()) {
            throw new IllegalArgumentException("Template name is required.");
        }
        String scope = normalizeScope(request.recipientScope());
        if ("DEPARTMENT".equals(scope) && request.recipientDepartmentId() == null) {
            throw new IllegalArgumentException("Recipient department is required for department-scoped templates.");
        }
        if (request.questions() == null || request.questions().isEmpty()) {
            throw new IllegalArgumentException("At least one template question is required.");
        }
        for (SurveyTemplateQuestionRequest question : request.questions()) {
            if (question == null || question.content() == null || question.content().trim().isEmpty()) {
                throw new IllegalArgumentException("All template questions must have content.");
            }
            normalizeType(question.type());
            if (question.questionBankEntryId() != null && !questionBankRepository.existsById(question.questionBankEntryId())) {
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

    private SurveyTemplateResponse toResponse(SurveyTemplateEntity entity) {
        return new SurveyTemplateResponse(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getSuggestedTitle(),
                entity.getSuggestedSurveyDescription(),
                entity.getRecipientScope(),
                entity.getRecipientDepartmentId(),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getQuestions().stream()
                        .map(question -> new SurveyTemplateQuestionResponse(
                                question.getId(),
                                question.getQuestionBankEntryId(),
                                question.getContent(),
                                question.getType(),
                                question.getDisplayOrder()
                        ))
                        .toList()
        );
    }
}
