package com.ttcs.backend.application.domain.service;

import com.ttcs.backend.application.domain.model.EvaluatorType;
import com.ttcs.backend.application.domain.model.Question;
import com.ttcs.backend.application.domain.model.QuestionType;
import com.ttcs.backend.application.domain.model.SubjectType;
import com.ttcs.backend.application.domain.model.Survey;
import com.ttcs.backend.application.domain.model.SurveyAssignment;
import com.ttcs.backend.application.domain.model.SurveyRecipientScope;
import com.ttcs.backend.application.port.in.admin.CloseSurveyUseCase;
import com.ttcs.backend.application.port.in.admin.GetManagedSurveyDetailUseCase;
import com.ttcs.backend.application.port.in.admin.GetManagedSurveysUseCase;
import com.ttcs.backend.application.port.in.admin.SurveyManagementActionResult;
import com.ttcs.backend.application.port.in.admin.SurveyManagementDetailResult;
import com.ttcs.backend.application.port.in.admin.SurveyManagementQuestionResult;
import com.ttcs.backend.application.port.in.admin.SurveyManagementSummaryResult;
import com.ttcs.backend.application.port.in.admin.SetSurveyHiddenUseCase;
import com.ttcs.backend.application.port.in.admin.UpdateSurveyCommand;
import com.ttcs.backend.application.port.in.admin.UpdateSurveyQuestionCommand;
import com.ttcs.backend.application.port.in.admin.UpdateSurveyUseCase;
import com.ttcs.backend.application.port.out.LoadQuestionPort;
import com.ttcs.backend.application.port.out.LoadSurveyAssignmentPort;
import com.ttcs.backend.application.port.out.LoadSurveyPort;
import com.ttcs.backend.application.port.out.LoadSurveyResponsePort;
import com.ttcs.backend.application.port.out.SaveQuestionPort;
import com.ttcs.backend.application.port.out.SaveSurveyAssignmentPort;
import com.ttcs.backend.application.port.out.SaveSurveyPort;
import com.ttcs.backend.common.UseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@UseCase
@RequiredArgsConstructor
public class AdminSurveyManagementService implements
        GetManagedSurveysUseCase,
        GetManagedSurveyDetailUseCase,
        UpdateSurveyUseCase,
        SetSurveyHiddenUseCase,
        CloseSurveyUseCase {

    private final LoadSurveyPort loadSurveyPort;
    private final SaveSurveyPort saveSurveyPort;
    private final LoadQuestionPort loadQuestionPort;
    private final SaveQuestionPort saveQuestionPort;
    private final LoadSurveyAssignmentPort loadSurveyAssignmentPort;
    private final SaveSurveyAssignmentPort saveSurveyAssignmentPort;
    private final LoadSurveyResponsePort loadSurveyResponsePort;

    @Override
    @Transactional(readOnly = true)
    public List<SurveyManagementSummaryResult> getSurveys() {
        return loadSurveyPort.loadAll().stream()
                .map(this::toSummary)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SurveyManagementDetailResult getSurvey(Integer surveyId) {
        Survey survey = loadSurveyPort.loadById(surveyId).orElseThrow(() -> new IllegalArgumentException("SURVEY_NOT_FOUND"));
        RecipientInfo recipient = recipientInfo(surveyId);
        long responseCount = loadSurveyResponsePort.countBySurveyId(surveyId);
        return new SurveyManagementDetailResult(
                survey.getId(),
                survey.getTitle(),
                survey.getDescription(),
                survey.getStartDate(),
                survey.getEndDate(),
                survey.status().name(),
                survey.isHidden(),
                recipient.scope().name(),
                recipient.departmentId(),
                responseCount,
                loadQuestionPort.loadBySurveyId(surveyId).stream()
                        .map(question -> new SurveyManagementQuestionResult(question.getId(), question.getContent(), question.getType().name()))
                        .toList()
        );
    }

    @Override
    @Transactional
    public SurveyManagementActionResult updateSurvey(UpdateSurveyCommand command) {
        if (command == null || command.surveyId() == null || isBlank(command.title())) {
            return SurveyManagementActionResult.fail("INVALID_INPUT", "Survey title is required.");
        }

        Survey existing = loadSurveyPort.loadById(command.surveyId()).orElse(null);
        if (existing == null) {
            return SurveyManagementActionResult.fail("SURVEY_NOT_FOUND", "Survey was not found.");
        }
        if (command.startDate() != null && command.endDate() != null && command.endDate().isBefore(command.startDate())) {
            return SurveyManagementActionResult.fail("INVALID_INPUT", "End date must be after start date.");
        }

        long responseCount = loadSurveyResponsePort.countBySurveyId(command.surveyId());
        if (responseCount > 0 && command.questions() != null && !command.questions().isEmpty()) {
            SurveyManagementDetailResult current = getSurvey(command.surveyId());
            boolean changedQuestions = current.questions().size() != command.questions().size()
                    || current.questions().stream().map(SurveyManagementQuestionResult::content).toList()
                    .equals(command.questions().stream().map(UpdateSurveyQuestionCommand::content).toList()) == false
                    || current.questions().stream().map(SurveyManagementQuestionResult::type).toList()
                    .equals(command.questions().stream().map(UpdateSurveyQuestionCommand::type).toList()) == false;
            if (changedQuestions) {
                return SurveyManagementActionResult.fail("SURVEY_LOCKED", "Questions cannot be changed after responses exist.");
            }
        }
        if (responseCount > 0) {
            RecipientInfo currentRecipient = recipientInfo(command.surveyId());
            if (currentRecipient.scope() != command.recipientScope()
                    || (currentRecipient.departmentId() == null ? command.recipientDepartmentId() != null : !currentRecipient.departmentId().equals(command.recipientDepartmentId()))) {
                return SurveyManagementActionResult.fail("SURVEY_LOCKED", "Recipients cannot be changed after responses exist.");
            }
        }

        Survey updatedSurvey = new Survey(
                existing.getId(),
                command.title().trim(),
                command.description(),
                command.startDate(),
                command.endDate(),
                existing.getCreatedBy(),
                existing.isHidden()
        );
        saveSurveyPort.save(updatedSurvey);

        if (responseCount == 0 && command.questions() != null && !command.questions().isEmpty()) {
            saveQuestionPort.replaceSurveyQuestions(
                    command.surveyId(),
                    command.questions().stream()
                            .map(question -> new Question(
                                    null,
                                    command.surveyId(),
                                    question.content(),
                                    QuestionType.valueOf(question.type())
                            ))
                            .toList()
            );

            saveSurveyAssignmentPort.replaceAssignments(
                    command.surveyId(),
                    List.of(toAssignment(updatedSurvey, command.recipientScope(), command.recipientDepartmentId()))
            );
        }

        return SurveyManagementActionResult.ok("SURVEY_UPDATED", "Survey updated successfully.");
    }

    @Override
    @Transactional
    public SurveyManagementActionResult setHidden(Integer surveyId, boolean hidden) {
        Survey survey = loadSurveyPort.loadById(surveyId).orElse(null);
        if (survey == null) {
            return SurveyManagementActionResult.fail("SURVEY_NOT_FOUND", "Survey was not found.");
        }

        saveSurveyPort.save(new Survey(
                survey.getId(),
                survey.getTitle(),
                survey.getDescription(),
                survey.getStartDate(),
                survey.getEndDate(),
                survey.getCreatedBy(),
                hidden
        ));
        return SurveyManagementActionResult.ok(hidden ? "SURVEY_HIDDEN" : "SURVEY_VISIBLE", hidden ? "Survey hidden successfully." : "Survey is visible again.");
    }

    @Override
    @Transactional
    public SurveyManagementActionResult closeSurvey(Integer surveyId) {
        Survey survey = loadSurveyPort.loadById(surveyId).orElse(null);
        if (survey == null) {
            return SurveyManagementActionResult.fail("SURVEY_NOT_FOUND", "Survey was not found.");
        }

        saveSurveyPort.save(new Survey(
                survey.getId(),
                survey.getTitle(),
                survey.getDescription(),
                survey.getStartDate(),
                LocalDateTime.now(),
                survey.getCreatedBy(),
                survey.isHidden()
        ));
        return SurveyManagementActionResult.ok("SURVEY_CLOSED", "Survey closed successfully.");
    }

    private SurveyManagementSummaryResult toSummary(Survey survey) {
        RecipientInfo recipient = recipientInfo(survey.getId());
        return new SurveyManagementSummaryResult(
                survey.getId(),
                survey.getTitle(),
                survey.getDescription(),
                survey.getStartDate(),
                survey.getEndDate(),
                survey.status().name(),
                survey.isHidden(),
                recipient.scope().name(),
                recipient.departmentId(),
                loadSurveyResponsePort.countBySurveyId(survey.getId())
        );
    }

    private RecipientInfo recipientInfo(Integer surveyId) {
        List<SurveyAssignment> assignments = loadSurveyAssignmentPort.loadBySurveyId(surveyId);
        if (assignments.isEmpty()) {
            return new RecipientInfo(SurveyRecipientScope.ALL_STUDENTS, null);
        }

        SurveyAssignment assignment = assignments.getFirst();
        if (assignment.getEvaluatorType() == EvaluatorType.STUDENT && assignment.getSubjectType() == SubjectType.DEPARTMENT) {
            return new RecipientInfo(SurveyRecipientScope.DEPARTMENT, assignment.getSubjectValue());
        }
        return new RecipientInfo(SurveyRecipientScope.ALL_STUDENTS, null);
    }

    private SurveyAssignment toAssignment(Survey survey, SurveyRecipientScope scope, Integer departmentId) {
        if (scope == SurveyRecipientScope.DEPARTMENT) {
            return new SurveyAssignment(null, survey, EvaluatorType.STUDENT, null, SubjectType.DEPARTMENT, departmentId);
        }
        return new SurveyAssignment(null, survey, EvaluatorType.STUDENT, null, SubjectType.ALL, null);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private record RecipientInfo(SurveyRecipientScope scope, Integer departmentId) {
    }
}
