package com.ttcs.backend.application.port.out;

import com.ttcs.backend.application.domain.model.SurveyAssignment;

import java.util.List;

public interface SaveSurveyAssignmentPort {
    void replaceAssignments(Integer surveyId, List<SurveyAssignment> assignments);
}
