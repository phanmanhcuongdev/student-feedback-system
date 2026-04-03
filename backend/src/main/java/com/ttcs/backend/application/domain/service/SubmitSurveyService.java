package com.ttcs.backend.application.domain.service;

import com.ttcs.backend.application.domain.model.*;
import com.ttcs.backend.application.port.in.SubmitSurveyUseCase;
import com.ttcs.backend.application.port.in.command.SubmitSurveyAnswerCommand;
import com.ttcs.backend.application.port.in.command.SubmitSurveyCommand;
import com.ttcs.backend.application.port.in.result.SubmitSurveyResult;
import com.ttcs.backend.application.port.in.result.SubmitSurveyResultCode;
import com.ttcs.backend.application.port.out.*;
import com.ttcs.backend.common.UseCase;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@UseCase
@RequiredArgsConstructor
@Transactional
public class SubmitSurveyService implements SubmitSurveyUseCase {

    private final LoadSurveyPort loadSurveyPort;
    private final LoadStudentPort loadStudentPort;
    private final LoadQuestionPort loadQuestionPort;
    private final LoadSurveyResponsePort loadSurveyResponsePort;
    private final SaveSurveyResponsePort saveSurveyResponsePort;
    private final SaveResponseDetailPort saveResponseDetailPort;

    @Override
    public SubmitSurveyResult submitSurvey(SubmitSurveyCommand command) {
        if (command == null
                || command.surveyId() == null
                || command.studentId() == null
                || command.answers() == null
                || command.answers().isEmpty()) {
            return SubmitSurveyResult.fail(
                    SubmitSurveyResultCode.INVALID_INPUT,
                    "Invalid submit request"
            );
        }

        Survey survey = loadSurveyPort.loadById(command.surveyId()).orElse(null);
        if (survey == null) {
            return SubmitSurveyResult.fail(
                    SubmitSurveyResultCode.SURVEY_NOT_FOUND,
                    "Survey not found"
            );
        }

        if (!survey.isOpen()) {
            return SubmitSurveyResult.fail(
                    SubmitSurveyResultCode.SURVEY_CLOSED,
                    "Survey is not open for submission"
            );
        }

        Student student = loadStudentPort.loadById(command.studentId()).orElse(null);
        if (student == null) {
            return SubmitSurveyResult.fail(
                    SubmitSurveyResultCode.STUDENT_NOT_FOUND,
                    "Student not found"
            );
        }

        boolean alreadySubmitted =
                loadSurveyResponsePort.existsBySurveyIdAndStudentId(command.surveyId(), command.studentId());

        if (alreadySubmitted) {
            return SubmitSurveyResult.fail(
                    SubmitSurveyResultCode.ALREADY_SUBMITTED,
                    "Student has already submitted this survey"
            );
        }

        List<Question> surveyQuestions = loadQuestionPort.loadBySurveyId(command.surveyId());
        ValidationResult validationResult = validateAnswers(command.answers(), surveyQuestions);
        if (!validationResult.valid()) {
            return SubmitSurveyResult.fail(validationResult.code(), validationResult.message());
        }

        SurveyResponse surveyResponse = new SurveyResponse(
                null,
                student,
                null,
                survey,
                LocalDateTime.now()
        );

        SurveyResponse savedSurveyResponse = saveSurveyResponsePort.save(surveyResponse);

        List<ResponseDetail> responseDetails = validationResult.responseDetails(savedSurveyResponse);
        saveResponseDetailPort.saveAll(responseDetails);

        return SubmitSurveyResult.success("Submit survey successfully");
    }

    private ValidationResult validateAnswers(List<SubmitSurveyAnswerCommand> answers, List<Question> surveyQuestions) {
        if (surveyQuestions.isEmpty()) {
            return ValidationResult.invalid("Survey has no questions");
        }

        Map<Integer, Question> questionById = new HashMap<>();
        for (Question question : surveyQuestions) {
            questionById.put(question.getId(), question);
        }

        Set<Integer> seenQuestionIds = new HashSet<>();
        List<ValidatedAnswer> validatedAnswers = new ArrayList<>();

        for (SubmitSurveyAnswerCommand answer : answers) {
            if (answer == null || answer.questionId() == null) {
                return ValidationResult.invalid("Question id is required");
            }

            if (!seenQuestionIds.add(answer.questionId())) {
                return ValidationResult.invalid("Each question can only be answered once");
            }

            Question question = questionById.get(answer.questionId());
            if (question == null) {
                return ValidationResult.invalid("Question does not belong to this survey");
            }

            String validationError = validateAnswerValue(question, answer);
            if (validationError != null) {
                return ValidationResult.invalid(validationError);
            }

            validatedAnswers.add(new ValidatedAnswer(question, answer.rating(), normalizeComment(answer.comment())));
        }

        if (seenQuestionIds.size() != surveyQuestions.size()) {
            return ValidationResult.invalid("All survey questions must be answered");
        }

        return ValidationResult.valid(validatedAnswers);
    }

    private String validateAnswerValue(Question question, SubmitSurveyAnswerCommand answer) {
        if (question.isRating()) {
            if (answer.comment() != null && !answer.comment().isBlank()) {
                return "Rating question does not accept comment";
            }

            if (answer.rating() == null) {
                return "Rating is required for rating question";
            }

            if (answer.rating() < 1 || answer.rating() > 5) {
                return "Rating must be between 1 and 5";
            }

            return null;
        }

        if (answer.rating() != null) {
            return "Text question does not accept rating";
        }

        String comment = normalizeComment(answer.comment());
        if (comment == null) {
            return "Comment is required for text question";
        }

        return null;
    }

    private String normalizeComment(String comment) {
        if (comment == null) {
            return null;
        }

        String trimmed = comment.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private record ValidatedAnswer(Question question, Integer rating, String comment) {
    }

    private record ValidationResult(
            boolean valid,
            SubmitSurveyResultCode code,
            String message,
            List<ValidatedAnswer> answers
    ) {
        private static ValidationResult valid(List<ValidatedAnswer> answers) {
            return new ValidationResult(true, null, null, answers);
        }

        private static ValidationResult invalid(String message) {
            return new ValidationResult(false, SubmitSurveyResultCode.INVALID_INPUT, message, List.of());
        }

        private List<ResponseDetail> responseDetails(SurveyResponse surveyResponse) {
            return answers.stream()
                    .map(answer -> new ResponseDetail(
                            null,
                            surveyResponse,
                            answer.question(),
                            answer.rating(),
                            answer.comment()
                    ))
                    .toList();
        }
    }
}
