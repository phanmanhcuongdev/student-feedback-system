package com.ttcs.backend.application.domain.service;

import com.ttcs.backend.application.domain.model.EvaluatorType;
import com.ttcs.backend.application.domain.model.Question;
import com.ttcs.backend.application.domain.model.SubjectType;
import com.ttcs.backend.application.domain.model.Survey;
import com.ttcs.backend.application.domain.model.SurveyAssignment;
import com.ttcs.backend.application.domain.model.SurveyLifecycleState;
import com.ttcs.backend.application.domain.model.SurveyRecipientScope;
import com.ttcs.backend.application.port.in.CreateSurveyUseCase;
import com.ttcs.backend.application.port.in.command.CreateSurveyCommand;
import com.ttcs.backend.application.port.out.SaveQuestionPort;
import com.ttcs.backend.application.port.out.SaveSurveyAssignmentPort;
import com.ttcs.backend.application.port.out.SaveSurveyPort;
import com.ttcs.backend.application.port.out.ai.SendTranslationTaskPort;
import com.ttcs.backend.application.port.out.ai.TranslationTaskCommand;
import com.ttcs.backend.common.UseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

@UseCase
public class CreateSurveyService implements CreateSurveyUseCase {

    private final SaveSurveyPort saveSurveyPort;
    private final SaveQuestionPort saveQuestionPort;
    private final SaveSurveyAssignmentPort saveSurveyAssignmentPort;
    private final SendTranslationTaskPort sendTranslationTaskPort;

    @Autowired
    public CreateSurveyService(
            SaveSurveyPort saveSurveyPort,
            SaveQuestionPort saveQuestionPort,
            SaveSurveyAssignmentPort saveSurveyAssignmentPort,
            SendTranslationTaskPort sendTranslationTaskPort
    ) {
        this.saveSurveyPort = saveSurveyPort;
        this.saveQuestionPort = saveQuestionPort;
        this.saveSurveyAssignmentPort = saveSurveyAssignmentPort;
        this.sendTranslationTaskPort = sendTranslationTaskPort;
    }

    public CreateSurveyService(
            SaveSurveyPort saveSurveyPort,
            SaveQuestionPort saveQuestionPort,
            SaveSurveyAssignmentPort saveSurveyAssignmentPort
    ) {
        this(saveSurveyPort, saveQuestionPort, saveSurveyAssignmentPort, command -> {
        });
    }

    @Override
    @Transactional
    public Integer createSurvey(CreateSurveyCommand command) {
        if (command == null || isBlank(command.title())) {
            throw new IllegalArgumentException("Survey title is required");
        }
        if (command.recipientScope() == null) {
            throw new IllegalArgumentException("Recipient scope is required");
        }
        if (command.startDate() != null && command.endDate() != null && command.endDate().isBefore(command.startDate())) {
            throw new IllegalArgumentException("End date must be after start date");
        }
        if (command.questions() == null || command.questions().isEmpty()) {
            throw new IllegalArgumentException("At least one question is required");
        }
        if (command.questions().stream().anyMatch(question -> question == null || isBlank(question.content()))) {
            throw new IllegalArgumentException("All questions must have content");
        }
        if (command.recipientScope() == SurveyRecipientScope.DEPARTMENT && command.recipientDepartmentId() == null) {
            throw new IllegalArgumentException("Recipient department is required for department scope");
        }

        Survey savedSurvey = saveSurveyPort.save(new Survey(
                null,
                command.title().trim(),
                command.description(),
                command.startDate(),
                command.endDate(),
                command.createdBy(),
                false,
                SurveyLifecycleState.DRAFT
        ));

        List<Question> savedQuestions = saveQuestionPort.saveAllReturning(command.questions().stream()
                .map(qCmd -> new Question(
                        null,
                        savedSurvey.getId(),
                        qCmd.content(),
                        qCmd.type(),
                        qCmd.questionBankEntryId()
                ))
                .toList());
        sendQuestionTranslationTasksAfterCommit(savedQuestions, normalizeLanguage(command.targetLang()));

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

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void sendQuestionTranslationTasksAfterCommit(List<Question> questions, String targetLang) {
        if (questions == null || questions.isEmpty()) {
            return;
        }
        Runnable dispatch = () -> questions.forEach(question -> sendTranslationTaskPort.send(new TranslationTaskCommand(
                question.getId(),
                "QUESTION",
                question.getContent(),
                targetLang
        )));
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            dispatch.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                dispatch.run();
            }
        });
    }

    private String normalizeLanguage(String value) {
        if (isBlank(value)) {
            return "vi";
        }
        String language = value.split(",")[0].trim().split("-")[0].toLowerCase();
        return "en".equals(language) ? "en" : "vi";
    }
}
