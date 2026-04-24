package com.ttcs.backend.adapter.out.persistence.survey;

import com.ttcs.backend.application.domain.model.SurveyLifecycleState;
import com.ttcs.backend.application.domain.model.Survey;

public final class SurveyMapper {

    private SurveyMapper() {
    }

    public static Survey toDomain(SurveyEntity entity) {
        if (entity == null) {
            return null;
        }

        return new Survey(
                entity.getId(),
                entity.getTitle(),
                entity.getTitleVi(),
                entity.getTitleEn(),
                entity.getDescription(),
                entity.getDescriptionVi(),
                entity.getDescriptionEn(),
                entity.getSourceLang(),
                entity.isAutoTranslated(),
                entity.getModelInfo(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getCreatedBy() != null ? entity.getCreatedBy().getId() : null,
                entity.isHidden(),
                parseLifecycleState(entity.getLifecycleState())
        );
    }

    public static SurveyEntity toEntity(Survey domain) {
        if (domain == null) return null;

        SurveyEntity entity = new SurveyEntity();
        entity.setId(domain.getId());
        entity.setTitle(domain.getTitle());
        entity.setTitleVi(domain.getTitleVi());
        entity.setTitleEn(domain.getTitleEn());
        entity.setDescription(domain.getDescription());
        entity.setDescriptionVi(domain.getDescriptionVi());
        entity.setDescriptionEn(domain.getDescriptionEn());
        entity.setSourceLang(domain.getSourceLang());
        entity.setAutoTranslated(domain.isAutoTranslated());
        entity.setModelInfo(domain.getModelInfo());
        entity.setStartDate(domain.getStartDate());
        entity.setEndDate(domain.getEndDate());
        entity.setHidden(domain.isHidden());
        entity.setLifecycleState(domain.getLifecycleState().name());

        return entity;
    }

    private static SurveyLifecycleState parseLifecycleState(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return SurveyLifecycleState.DRAFT;
        }
        return SurveyLifecycleState.valueOf(rawValue.trim().toUpperCase());
    }
}
