package com.ttcs.backend.adapter.out.persistence;

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
                entity.getCreatedBy().getUser().getUser_id()
        );
    }
}