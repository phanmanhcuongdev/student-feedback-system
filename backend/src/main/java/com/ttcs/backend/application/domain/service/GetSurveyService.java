package com.ttcs.backend.application.domain.service;

import com.ttcs.backend.application.domain.exception.SurveyNotFoundException;
import com.ttcs.backend.application.domain.model.EvaluatorType;
import com.ttcs.backend.application.domain.model.Student;
import com.ttcs.backend.application.domain.model.SubjectType;
import com.ttcs.backend.application.domain.model.Survey;
import com.ttcs.backend.application.domain.model.SurveyAssignment;
import com.ttcs.backend.application.port.in.GetSurveyUseCase;
import com.ttcs.backend.application.port.in.result.SurveySummaryResult;
import com.ttcs.backend.application.port.out.LoadSurveyAssignmentPort;
import com.ttcs.backend.application.port.out.LoadSurveyPort;
import com.ttcs.backend.application.port.out.auth.LoadStudentByIdPort;
import com.ttcs.backend.common.UseCase;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
@UseCase
public class GetSurveyService implements GetSurveyUseCase {

    private final LoadSurveyPort loadSurveyPort;
    private final LoadSurveyAssignmentPort loadSurveyAssignmentPort;
    private final LoadStudentByIdPort loadStudentByIdPort;

    @Override
    public SurveySummaryResult getSurveyById(Integer surveyId, Integer studentUserId) {
        Survey survey = loadSurveyPort.loadById(surveyId)
                .orElseThrow(() -> new SurveyNotFoundException(surveyId));
        Student student = loadStudentByIdPort.loadByUserId(studentUserId)
                .orElseThrow(() -> new SurveyNotFoundException(surveyId));
        if (survey.isHidden() || !isAssignedToStudent(surveyId, student)) {
            throw new SurveyNotFoundException(surveyId);
        }

        return new SurveySummaryResult(
                survey.getId(),
                survey.getTitle(),
                survey.getDescription(),
                survey.getStartDate(),
                survey.getEndDate(),
                survey.getCreatedBy(),
                survey.status()
        );
    }

    @Override
    public List<SurveySummaryResult> getAllSurveys(Integer studentUserId) {
        Student student = loadStudentByIdPort.loadByUserId(studentUserId)
                .orElseThrow(() -> new IllegalArgumentException("Student profile not found"));
        return loadSurveyPort.loadAll()
                .stream()
                .filter(survey -> !survey.isHidden())
                .filter(survey -> isAssignedToStudent(survey.getId(), student))
                .map(survey -> new SurveySummaryResult(
                        survey.getId(),
                        survey.getTitle(),
                        survey.getDescription(),
                        survey.getStartDate(),
                        survey.getEndDate(),
                        survey.getCreatedBy(),
                        survey.status()
                ))
                .toList();
    }

    private boolean isAssignedToStudent(Integer surveyId, Student student) {
        List<SurveyAssignment> assignments = loadSurveyAssignmentPort.loadBySurveyId(surveyId);
        if (assignments.isEmpty()) {
            return true;
        }

        return assignments.stream().anyMatch(assignment ->
                assignment.getEvaluatorType() == EvaluatorType.STUDENT
                        && (assignment.getSubjectType() == SubjectType.ALL
                        || (assignment.getSubjectType() == SubjectType.DEPARTMENT
                        && student.getDepartment() != null
                        && assignment.getSubjectValue() != null
                        && assignment.getSubjectValue().equals(student.getDepartment().getId())))
        );
    }
}
