package com.ttcs.backend.application.domain.service;

import com.ttcs.backend.application.domain.model.EvaluatorType;
import com.ttcs.backend.application.domain.model.Question;
import com.ttcs.backend.application.domain.model.QuestionType;
import com.ttcs.backend.application.domain.model.SubjectType;
import com.ttcs.backend.application.domain.model.Survey;
import com.ttcs.backend.application.domain.model.SurveyAssignment;
import com.ttcs.backend.application.domain.model.SurveyRecipientScope;
import com.ttcs.backend.application.port.in.admin.SurveyManagementActionResult;
import com.ttcs.backend.application.port.in.admin.UpdateSurveyCommand;
import com.ttcs.backend.application.port.in.admin.UpdateSurveyQuestionCommand;
import com.ttcs.backend.application.port.out.LoadQuestionPort;
import com.ttcs.backend.application.port.out.LoadSurveyAssignmentPort;
import com.ttcs.backend.application.port.out.LoadSurveyPort;
import com.ttcs.backend.application.port.out.LoadSurveyResponsePort;
import com.ttcs.backend.application.port.out.SaveQuestionPort;
import com.ttcs.backend.application.port.out.SaveSurveyAssignmentPort;
import com.ttcs.backend.application.port.out.SaveSurveyPort;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdminSurveyManagementServiceTest {

    @Test
    void shouldUpdateSurveyWhenNoResponsesExist() {
        InMemorySurveyState state = new InMemorySurveyState(0);
        AdminSurveyManagementService service = new AdminSurveyManagementService(
                new SurveyPort(state),
                new SurveyPort(state),
                new QuestionPort(state),
                new QuestionPort(state),
                new AssignmentPort(state),
                new AssignmentPort(state),
                new ResponsePort(state)
        );

        SurveyManagementActionResult result = service.updateSurvey(new UpdateSurveyCommand(
                1,
                "Updated Survey",
                "Updated description",
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(5),
                List.of(new UpdateSurveyQuestionCommand("Updated question", "TEXT")),
                SurveyRecipientScope.DEPARTMENT,
                2
        ));

        assertTrue(result.success());
        assertEquals("Updated Survey", state.survey.getTitle());
        assertEquals(1, state.questions.size());
        assertEquals(2, state.assignments.getFirst().getSubjectValue());
    }

    @Test
    void shouldRejectQuestionChangeAfterResponsesExist() {
        InMemorySurveyState state = new InMemorySurveyState(3);
        AdminSurveyManagementService service = new AdminSurveyManagementService(
                new SurveyPort(state),
                new SurveyPort(state),
                new QuestionPort(state),
                new QuestionPort(state),
                new AssignmentPort(state),
                new AssignmentPort(state),
                new ResponsePort(state)
        );

        SurveyManagementActionResult result = service.updateSurvey(new UpdateSurveyCommand(
                1,
                "Initial Survey",
                "Initial description",
                state.survey.getStartDate(),
                state.survey.getEndDate(),
                List.of(new UpdateSurveyQuestionCommand("Changed question", "TEXT")),
                SurveyRecipientScope.ALL_STUDENTS,
                null
        ));

        assertFalse(result.success());
        assertEquals("SURVEY_LOCKED", result.code());
    }

    @Test
    void shouldHideSurvey() {
        InMemorySurveyState state = new InMemorySurveyState(0);
        AdminSurveyManagementService service = new AdminSurveyManagementService(
                new SurveyPort(state),
                new SurveyPort(state),
                new QuestionPort(state),
                new QuestionPort(state),
                new AssignmentPort(state),
                new AssignmentPort(state),
                new ResponsePort(state)
        );

        SurveyManagementActionResult result = service.setHidden(1, true);

        assertTrue(result.success());
        assertTrue(state.survey.isHidden());
    }

    @Test
    void shouldCloseSurvey() {
        InMemorySurveyState state = new InMemorySurveyState(0);
        AdminSurveyManagementService service = new AdminSurveyManagementService(
                new SurveyPort(state),
                new SurveyPort(state),
                new QuestionPort(state),
                new QuestionPort(state),
                new AssignmentPort(state),
                new AssignmentPort(state),
                new ResponsePort(state)
        );

        SurveyManagementActionResult result = service.closeSurvey(1);

        assertTrue(result.success());
        assertTrue(state.survey.getEndDate().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    private static final class InMemorySurveyState {
        private Survey survey = new Survey(1, "Initial Survey", "Initial description", LocalDateTime.now().minusDays(2), LocalDateTime.now().plusDays(2), 1, false);
        private List<Question> questions = new ArrayList<>(List.of(
                new Question(1, 1, "Initial question", QuestionType.RATING)
        ));
        private List<SurveyAssignment> assignments = new ArrayList<>(List.of(
                new SurveyAssignment(1, survey, EvaluatorType.STUDENT, null, SubjectType.ALL, null)
        ));
        private final long responseCount;

        private InMemorySurveyState(long responseCount) {
            this.responseCount = responseCount;
        }
    }

    private static final class SurveyPort implements LoadSurveyPort, SaveSurveyPort {
        private final InMemorySurveyState state;

        private SurveyPort(InMemorySurveyState state) {
            this.state = state;
        }

        @Override
        public Optional<Survey> loadById(Integer surveyId) {
            return Optional.of(state.survey);
        }

        @Override
        public List<Survey> loadAll() {
            return List.of(state.survey);
        }

        @Override
        public Survey save(Survey survey) {
            state.survey = survey;
            return survey;
        }
    }

    private static final class QuestionPort implements LoadQuestionPort, SaveQuestionPort {
        private final InMemorySurveyState state;

        private QuestionPort(InMemorySurveyState state) {
            this.state = state;
        }

        @Override
        public List<Question> loadBySurveyId(Integer surveyId) {
            return List.copyOf(state.questions);
        }

        @Override
        public void saveAll(List<Question> questions) {
            state.questions = new ArrayList<>(questions);
        }

        @Override
        public void replaceSurveyQuestions(Integer surveyId, List<Question> questions) {
            state.questions = new ArrayList<>(questions);
        }
    }

    private static final class AssignmentPort implements LoadSurveyAssignmentPort, SaveSurveyAssignmentPort {
        private final InMemorySurveyState state;

        private AssignmentPort(InMemorySurveyState state) {
            this.state = state;
        }

        @Override
        public List<SurveyAssignment> loadBySurveyId(Integer surveyId) {
            return List.copyOf(state.assignments);
        }

        @Override
        public void replaceAssignments(Integer surveyId, List<SurveyAssignment> assignments) {
            state.assignments = new ArrayList<>(assignments);
        }
    }

    private static final class ResponsePort implements LoadSurveyResponsePort {
        private final InMemorySurveyState state;

        private ResponsePort(InMemorySurveyState state) {
            this.state = state;
        }

        @Override
        public boolean existsBySurveyIdAndStudentId(Integer surveyId, Integer studentId) {
            return false;
        }

        @Override
        public long countBySurveyId(Integer surveyId) {
            return state.responseCount;
        }
    }
}
