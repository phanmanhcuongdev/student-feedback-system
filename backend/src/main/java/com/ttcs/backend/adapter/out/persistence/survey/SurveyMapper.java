package com.ttcs.backend.adapter.out.persistence.survey;

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
                entity.getDescription(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getCreatedBy() != null ? entity.getCreatedBy().getId() : null,
                entity.isHidden()
        );
    }

    public static SurveyEntity toEntity(Survey domain) {
        if (domain == null) return null;

        SurveyEntity entity = new SurveyEntity();
        entity.setId(domain.getId());
        entity.setTitle(domain.getTitle());
        entity.setDescription(domain.getDescription());
        entity.setStartDate(domain.getStartDate());
        entity.setEndDate(domain.getEndDate());
        entity.setHidden(domain.isHidden());

        return entity;
    }
}
