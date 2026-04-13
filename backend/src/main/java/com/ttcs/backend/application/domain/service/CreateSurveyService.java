package com.ttcs.backend.application.domain.service;

import com.ttcs.backend.application.domain.model.Question;
import com.ttcs.backend.application.domain.model.EvaluatorType;
import com.ttcs.backend.application.domain.model.SubjectType;
import com.ttcs.backend.application.domain.model.Survey;
import com.ttcs.backend.application.domain.model.SurveyAssignment;
import com.ttcs.backend.application.domain.model.SurveyRecipientScope;
import com.ttcs.backend.application.port.in.CreateSurveyUseCase;
import com.ttcs.backend.application.port.in.command.CreateSurveyCommand;
import com.ttcs.backend.application.port.out.SaveQuestionPort;
import com.ttcs.backend.application.port.out.SaveSurveyAssignmentPort;
import com.ttcs.backend.application.port.out.SaveSurveyPort;
import com.ttcs.backend.common.UseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@UseCase
@RequiredArgsConstructor
public class CreateSurveyService implements CreateSurveyUseCase {

    private final SaveSurveyPort saveSurveyPort;
    private final SaveQuestionPort saveQuestionPort;
    private final SaveSurveyAssignmentPort saveSurveyAssignmentPort;

    @Override
    @Transactional
    public Integer createSurvey(CreateSurveyCommand command) {
        // Create and save Survey
        Survey survey = new Survey(
                null,
                command.title(),
                command.description(),
                command.startDate(),
                command.endDate(),
                command.createdBy(),
                false
        );
        Survey savedSurvey = saveSurveyPort.save(survey);

        // Create and save Questions with the newly generated Survey ID
        if (command.questions() != null && !command.questions().isEmpty()) {
            List<Question> questionsToSave = command.questions().stream()
                    .map(qCmd -> new Question(
                            null,
                            savedSurvey.getId(),
                            qCmd.content(),
                            qCmd.type()
                    ))
                    .toList();
            saveQuestionPort.saveAll(questionsToSave);
        }

        saveSurveyAssignmentPort.replaceAssignments(
                savedSurvey.getId(),
                List.of(toAssignment(savedSurvey, command.recipientScope(), command.recipientDepartmentId()))
        );

        return savedSurvey.getId();
    }

    private SurveyAssignment toAssignment(Survey survey, SurveyRecipientScope scope, Integer departmentId) {
        if (scope == SurveyRecipientScope.DEPARTMENT) {
            return new SurveyAssignment(
                    null,
                    survey,
                    EvaluatorType.STUDENT,
                    null,
                    SubjectType.DEPARTMENT,
                    departmentId
            );
        }

        return new SurveyAssignment(
                null,
                survey,
                EvaluatorType.STUDENT,
                null,
                SubjectType.ALL,
                null
        );
    }
}
