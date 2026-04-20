package com.ttcs.backend.adapter.out.persistence.surveyassignment;

import com.ttcs.backend.adapter.out.persistence.survey.SurveyMapper;
import com.ttcs.backend.application.domain.model.EvaluatorType;
import com.ttcs.backend.application.domain.model.SubjectType;
import com.ttcs.backend.application.domain.model.SurveyAssignment;

public final class SurveyAssignmentMapper {

    private SurveyAssignmentMapper() {
    }

    public static SurveyAssignment toDomain(SurveyAssignmentEntity entity) {
        if (entity == null) {
            return null;
        }

        EvaluatorType evaluatorType = parseEvaluatorType(entity.getEvaluatorType());
        SubjectType subjectType = parseSubjectType(entity.getSubjectType());
        if (evaluatorType == null || subjectType == null) {
            return null;
        }

        return new SurveyAssignment(
                entity.getId(),
                SurveyMapper.toDomain(entity.getSurvey()),
                evaluatorType,
                entity.getEvaluatorValue(),
                subjectType,
                entity.getSubjectValue()
        );
    }

    private static EvaluatorType parseEvaluatorType(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }

        try {
            return EvaluatorType.valueOf(rawValue.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private static SubjectType parseSubjectType(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }

        try {
            String normalized = rawValue.trim().toUpperCase();
            return SubjectType.valueOf(normalized);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
