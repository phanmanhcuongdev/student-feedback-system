package com.ttcs.backend.application.domain.service;

import com.ttcs.backend.application.domain.model.Question;
import com.ttcs.backend.application.domain.model.QuestionType;
import com.ttcs.backend.application.domain.model.Survey;
import com.ttcs.backend.application.domain.model.SurveyAssignment;
import com.ttcs.backend.application.domain.model.SurveyLifecycleState;
import com.ttcs.backend.application.domain.model.SurveyRecipientScope;
import com.ttcs.backend.application.port.in.command.CreateQuestionCommand;
import com.ttcs.backend.application.port.in.command.CreateSurveyCommand;
import com.ttcs.backend.application.port.out.SaveQuestionPort;
import com.ttcs.backend.application.port.out.SaveSurveyAssignmentPort;
import com.ttcs.backend.application.port.out.SaveSurveyPort;
import com.ttcs.backend.application.port.out.ai.TranslationTaskCommand;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CreateSurveyServiceTest {

    @Test
    void shouldCreateSurveyAsDraftByDefault() {
        RecordingSaveSurveyPort saveSurveyPort = new RecordingSaveSurveyPort();
        CreateSurveyService service = new CreateSurveyService(
                saveSurveyPort,
                new NoOpSaveQuestionPort(),
                new NoOpSaveSurveyAssignmentPort()
        );

        Integer surveyId = service.createSurvey(new CreateSurveyCommand(
                "Teaching Feedback",
                "Draft survey",
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(3),
                1,
                List.of(new CreateQuestionCommand("Rate the class", QuestionType.RATING)),
                SurveyRecipientScope.ALL_STUDENTS,
                null
        ));

        assertNotNull(surveyId);
        assertNotNull(saveSurveyPort.lastSavedSurvey);
        assertEquals(SurveyLifecycleState.DRAFT, saveSurveyPort.lastSavedSurvey.getLifecycleState());
    }

    @Test
    void shouldRejectDepartmentScopeWithoutDepartmentId() {
        RecordingSaveSurveyPort saveSurveyPort = new RecordingSaveSurveyPort();
        CreateSurveyService service = new CreateSurveyService(
                saveSurveyPort,
                new NoOpSaveQuestionPort(),
                new NoOpSaveSurveyAssignmentPort()
        );

        assertThrows(IllegalArgumentException.class, () -> service.createSurvey(new CreateSurveyCommand(
                "Teaching Feedback",
                "Draft survey",
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(3),
                1,
                List.of(new CreateQuestionCommand("Rate the class", QuestionType.RATING)),
                SurveyRecipientScope.DEPARTMENT,
                null
        )));
    }

    @Test
    void shouldRejectBlankQuestionContent() {
        RecordingSaveSurveyPort saveSurveyPort = new RecordingSaveSurveyPort();
        CreateSurveyService service = new CreateSurveyService(
                saveSurveyPort,
                new NoOpSaveQuestionPort(),
                new NoOpSaveSurveyAssignmentPort()
        );

        assertThrows(IllegalArgumentException.class, () -> service.createSurvey(new CreateSurveyCommand(
                "Teaching Feedback",
                "Draft survey",
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(3),
                1,
                List.of(new CreateQuestionCommand("   ", QuestionType.TEXT)),
                SurveyRecipientScope.ALL_STUDENTS,
                null
        )));
    }

    @Test
    void shouldCopyQuestionBankSourceOntoSurveyQuestion() {
        RecordingSaveQuestionPort saveQuestionPort = new RecordingSaveQuestionPort();
        CreateSurveyService service = new CreateSurveyService(
                new RecordingSaveSurveyPort(),
                saveQuestionPort,
                new NoOpSaveSurveyAssignmentPort()
        );

        service.createSurvey(new CreateSurveyCommand(
                "Teaching Feedback",
                "Draft survey",
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(3),
                1,
                List.of(new CreateQuestionCommand("Rate the class", QuestionType.RATING, 42)),
                SurveyRecipientScope.ALL_STUDENTS,
                null
        ));

        assertEquals(42, saveQuestionPort.lastSavedQuestions.getFirst().getQuestionBankEntryId());
    }

    @Test
    void shouldSendSurveyTitleAndDescriptionTranslationTasks() {
        RecordingTranslationTaskPort translationTaskPort = new RecordingTranslationTaskPort();
        CreateSurveyService service = new CreateSurveyService(
                new RecordingSaveSurveyPort(),
                new RecordingSaveQuestionPort(),
                new NoOpSaveSurveyAssignmentPort(),
                translationTaskPort
        );

        service.createSurvey(new CreateSurveyCommand(
                "Teaching Feedback",
                "Draft survey",
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(3),
                1,
                List.of(new CreateQuestionCommand("Rate the class", QuestionType.RATING)),
                SurveyRecipientScope.ALL_STUDENTS,
                null,
                "vi"
        ));

        assertEquals(3, translationTaskPort.commands.size());
        assertEquals("SURVEY_TITLE", translationTaskPort.commands.get(0).entityType());
        assertEquals("Teaching Feedback", translationTaskPort.commands.get(0).content());
        assertEquals("SURVEY_DESCRIPTION", translationTaskPort.commands.get(1).entityType());
        assertEquals("Draft survey", translationTaskPort.commands.get(1).content());
    }

    private static final class RecordingSaveSurveyPort implements SaveSurveyPort {
        private Survey lastSavedSurvey;

        @Override
        public Survey save(Survey survey) {
            lastSavedSurvey = survey;
            return new Survey(1, survey.getTitle(), survey.getDescription(), survey.getStartDate(), survey.getEndDate(), survey.getCreatedBy(), survey.isHidden(), survey.getLifecycleState());
        }
    }

    private static final class NoOpSaveQuestionPort implements SaveQuestionPort {
        @Override
        public void saveAll(List<Question> questions) {
        }

        @Override
        public void replaceSurveyQuestions(Integer surveyId, List<Question> questions) {
        }
    }

    private static final class RecordingSaveQuestionPort implements SaveQuestionPort {
        private List<Question> lastSavedQuestions = List.of();

        @Override
        public void saveAll(List<Question> questions) {
            lastSavedQuestions = questions;
        }

        @Override
        public void replaceSurveyQuestions(Integer surveyId, List<Question> questions) {
            lastSavedQuestions = questions;
        }
    }

    private static final class NoOpSaveSurveyAssignmentPort implements SaveSurveyAssignmentPort {
        @Override
        public void replaceAssignments(Integer surveyId, List<SurveyAssignment> assignments) {
        }
    }

    private static final class RecordingTranslationTaskPort implements com.ttcs.backend.application.port.out.ai.SendTranslationTaskPort {
        private final List<TranslationTaskCommand> commands = new ArrayList<>();

        @Override
        public void send(TranslationTaskCommand command) {
            commands.add(command);
        }
    }
}
