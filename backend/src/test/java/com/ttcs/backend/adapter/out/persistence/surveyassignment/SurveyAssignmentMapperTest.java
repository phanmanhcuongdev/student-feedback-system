package com.ttcs.backend.adapter.out.persistence.surveyassignment;

import com.ttcs.backend.adapter.out.persistence.survey.SurveyEntity;
import com.ttcs.backend.application.domain.model.SurveyAssignment;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class SurveyAssignmentMapperTest {

    @Test
    void shouldMapLowercaseAssignmentTypesWithoutCrashing() {
        SurveyAssignmentEntity entity = new SurveyAssignmentEntity();
        entity.setId(1);
        entity.setSurvey(new SurveyEntity(1, "Title", "Desc", null, null, false, "DRAFT", null));
        entity.setEvaluatorType("student");
        entity.setSubjectType("department");
        entity.setSubjectValue(2);

        SurveyAssignment result = SurveyAssignmentMapper.toDomain(entity);

        assertNotNull(result);
        assertEquals(2, result.getSubjectValue());
        assertEquals("STUDENT", result.getEvaluatorType().name());
        assertEquals("DEPARTMENT", result.getSubjectType().name());
    }

    @Test
    void shouldReturnNullForInvalidAssignmentType() {
        SurveyAssignmentEntity entity = new SurveyAssignmentEntity();
        entity.setId(1);
        entity.setSurvey(new SurveyEntity(1, "Title", "Desc", null, null, false, "DRAFT", null));
        entity.setEvaluatorType("BROKEN");
        entity.setSubjectType("ALL");

        SurveyAssignment result = SurveyAssignmentMapper.toDomain(entity);

        assertNull(result);
    }
}
