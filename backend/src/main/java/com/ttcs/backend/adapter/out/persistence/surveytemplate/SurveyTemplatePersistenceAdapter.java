package com.ttcs.backend.adapter.out.persistence.surveytemplate;

import com.ttcs.backend.application.domain.model.SurveyTemplate;
import com.ttcs.backend.application.domain.model.SurveyTemplateQuestion;
import com.ttcs.backend.application.port.out.admin.ManageSurveyTemplatePort;
import com.ttcs.backend.application.port.out.admin.SurveyTemplateSearchPage;
import com.ttcs.backend.application.port.out.admin.SurveyTemplateSearchQuery;
import com.ttcs.backend.common.PersistenceAdapter;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@PersistenceAdapter
@RequiredArgsConstructor
public class SurveyTemplatePersistenceAdapter implements ManageSurveyTemplatePort {

    private final SurveyTemplateRepository surveyTemplateRepository;

    @Override
    public SurveyTemplateSearchPage loadPage(SurveyTemplateSearchQuery query) {
        int safePage = Math.max(query.page(), 0);
        int safeSize = Math.min(Math.max(query.size(), 1), 100);
        var result = surveyTemplateRepository.findAll(
                specification(query),
                PageRequest.of(
                        safePage,
                        safeSize,
                        Sort.by(Sort.Direction.DESC, "updatedAt").and(Sort.by(Sort.Direction.DESC, "createdAt"))
                )
        );

        return new SurveyTemplateSearchPage(
                result.getContent().stream().map(this::toDomain).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    @Override
    public Optional<SurveyTemplate> loadById(Integer id) {
        return surveyTemplateRepository.findById(id).map(this::toDomain);
    }

    @Override
    public SurveyTemplate save(SurveyTemplate template) {
        SurveyTemplateEntity entity = template.id() == null
                ? new SurveyTemplateEntity()
                : surveyTemplateRepository.findById(template.id())
                .orElseGet(SurveyTemplateEntity::new);
        applyToEntity(entity, template);
        return toDomain(surveyTemplateRepository.save(entity));
    }

    private Specification<SurveyTemplateEntity> specification(SurveyTemplateSearchQuery query) {
        return (root, ignoredQuery, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (query.keyword() != null && !query.keyword().isBlank()) {
                String pattern = "%" + query.keyword().trim().toLowerCase() + "%";
                predicates.add(builder.or(
                        builder.like(builder.lower(root.get("name")), pattern),
                        builder.like(builder.lower(root.get("description")), pattern),
                        builder.like(builder.lower(root.get("suggestedTitle")), pattern)
                ));
            }
            if (query.active() != null) {
                predicates.add(builder.equal(root.get("active"), query.active()));
            }
            return builder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private SurveyTemplate toDomain(SurveyTemplateEntity entity) {
        return new SurveyTemplate(
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
                        .map(this::toDomainQuestion)
                        .toList()
        );
    }

    private SurveyTemplateQuestion toDomainQuestion(SurveyTemplateQuestionEntity entity) {
        return new SurveyTemplateQuestion(
                entity.getId(),
                entity.getQuestionBankEntryId(),
                entity.getContent(),
                entity.getType(),
                entity.getDisplayOrder()
        );
    }

    private void applyToEntity(SurveyTemplateEntity entity, SurveyTemplate template) {
        entity.setId(template.id());
        entity.setName(template.name());
        entity.setDescription(template.description());
        entity.setSuggestedTitle(template.suggestedTitle());
        entity.setSuggestedSurveyDescription(template.suggestedSurveyDescription());
        entity.setRecipientScope(template.recipientScope());
        entity.setRecipientDepartmentId(template.recipientDepartmentId());
        entity.setActive(template.active());
        entity.setCreatedAt(template.createdAt());
        entity.setUpdatedAt(template.updatedAt());
        entity.getQuestions().clear();
        template.questions().forEach(question -> entity.getQuestions().add(toEntityQuestion(entity, question)));
    }

    private SurveyTemplateQuestionEntity toEntityQuestion(SurveyTemplateEntity template, SurveyTemplateQuestion question) {
        SurveyTemplateQuestionEntity entity = new SurveyTemplateQuestionEntity();
        entity.setId(question.id());
        entity.setTemplate(template);
        entity.setQuestionBankEntryId(question.questionBankEntryId());
        entity.setContent(question.content());
        entity.setType(question.type());
        entity.setDisplayOrder(question.displayOrder());
        return entity;
    }
}
