package com.ttcs.backend.adapter.out.persistence.surveyassignment;

import com.ttcs.backend.adapter.out.persistence.survey.SurveyMapper;
import com.ttcs.backend.application.domain.model.EvaluatorType;
import com.ttcs.backend.application.domain.model.SubjectType;
import com.ttcs.backend.application.domain.model.SurveyAssignment;

public final class SurveyAssignmentMapper {

    private SurveyAssignmentMapper() {
    }

    public static SurveyAssignment toDomain(SurveyAssignmentEntity entity) {
        return new SurveyAssignment(
                entity.getId(),
                SurveyMapper.toDomain(entity.getSurvey()),
                EvaluatorType.valueOf(entity.getEvaluatorType()),
                entity.getEvaluatorValue(),
                SubjectType.valueOf(entity.getSubjectType()),
                entity.getSubjectValue()
        );
    }
}
