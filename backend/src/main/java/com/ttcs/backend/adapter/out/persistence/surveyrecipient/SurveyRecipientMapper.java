package com.ttcs.backend.adapter.out.persistence.surveyrecipient;

import com.ttcs.backend.application.domain.model.SurveyRecipient;

public final class SurveyRecipientMapper {
    private SurveyRecipientMapper() {
    }

    public static SurveyRecipient toDomain(SurveyRecipientEntity entity) {
        if (entity == null) {
            return null;
        }
        return new SurveyRecipient(
                entity.getId(),
                entity.getSurvey().getId(),
                entity.getStudent().getId(),
                entity.getAssignedAt(),
                entity.getOpenedAt(),
                entity.getSubmittedAt()
        );
    }
}
