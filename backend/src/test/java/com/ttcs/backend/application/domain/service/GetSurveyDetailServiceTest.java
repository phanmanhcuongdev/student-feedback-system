package com.ttcs.backend.application.domain.service;

import com.ttcs.backend.application.domain.exception.SurveyNotFoundException;
import com.ttcs.backend.application.domain.model.Department;
import com.ttcs.backend.application.domain.model.Question;
import com.ttcs.backend.application.domain.model.QuestionType;
import com.ttcs.backend.application.domain.model.Survey;
import com.ttcs.backend.application.domain.model.SurveyLifecycleState;
import com.ttcs.backend.application.domain.model.SurveyRecipient;
import com.ttcs.backend.application.port.out.LoadQuestionPort;
import com.ttcs.backend.application.port.out.LoadStudentSurveysQuery;
import com.ttcs.backend.application.port.out.LoadSurveyPort;
import com.ttcs.backend.application.port.out.LoadSurveyRecipientPort;
import com.ttcs.backend.application.port.out.SaveSurveyRecipientPort;
import com.ttcs.backend.application.port.out.StudentSurveySearchPage;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GetSurveyDetailServiceTest {

    @Test
    void shouldRejectDraftSurveyDetailAccess() {
        Survey survey = new Survey(1, "Draft Survey", "Desc", LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(3), 1, false, SurveyLifecycleState.DRAFT);
        GetSurveyDetailService service = service(survey, false);

        assertThrows(SurveyNotFoundException.class, () -> service.getSurveyDetail(1, 3, "vi"));
    }

    @Test
    void shouldRejectArchivedSurveyDetailAccess() {
        Survey survey = new Survey(1, "Archived Survey", "Desc", LocalDateTime.now().minusDays(4), LocalDateTime.now().minusDays(1), 1, false, SurveyLifecycleState.ARCHIVED);
        GetSurveyDetailService service = service(survey, false);

        assertThrows(SurveyNotFoundException.class, () -> service.getSurveyDetail(1, 3, "vi"));
    }

    @Test
    void shouldReturnPublishedSurveyDetailForAssignedStudent() {
        Survey survey = new Survey(1, "Published Survey", "Desc", LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(2), 1, false, SurveyLifecycleState.PUBLISHED);
        GetSurveyDetailService service = service(survey, false);

        var result = service.getSurveyDetail(1, 3, "vi");

        assertEquals(1, result.id());
        assertEquals("Published Survey", result.title());
        assertEquals(1, result.questions().size());
        assertEquals("Danh gia giang vien", result.questions().getFirst().content());
    }

    private GetSurveyDetailService service(Survey survey, boolean hidden) {
        Survey effectiveSurvey = new Survey(
                survey.getId(),
                survey.getTitle(),
                survey.getDescription(),
                survey.getStartDate(),
                survey.getEndDate(),
                survey.getCreatedBy(),
                hidden,
                survey.getLifecycleState()
        );
        return new GetSurveyDetailService(
                new SurveyPort(effectiveSurvey),
                new QuestionPort(),
                new RecipientPort(),
                new RecipientPort()
        );
    }

    private static final class SurveyPort implements LoadSurveyPort {
        private final Survey survey;

        private SurveyPort(Survey survey) {
            this.survey = survey;
        }

        @Override
        public Optional<Survey> loadById(Integer surveyId) {
            return Optional.of(survey);
        }

        @Override
        public List<Survey> loadAll() {
            return List.of(survey);
        }

        @Override
        public StudentSurveySearchPage loadStudentSurveyPage(LoadStudentSurveysQuery query) {
            return new StudentSurveySearchPage(List.of(), 0, 0, 0, 0);
        }
    }

    private static final class QuestionPort implements LoadQuestionPort {
        @Override
        public List<Question> loadBySurveyId(Integer surveyId) {
            return List.of(new Question(
                    1,
                    surveyId,
                    "Rate the lecturer",
                    "Danh gia giang vien",
                    "en",
                    true,
                    "vi",
                    QuestionType.RATING,
                    null
            ));
        }
    }

    private static final class RecipientPort implements LoadSurveyRecipientPort, SaveSurveyRecipientPort {
        @Override
        public Optional<SurveyRecipient> loadBySurveyIdAndStudentId(Integer surveyId, Integer studentId) {
            return Optional.of(new SurveyRecipient(1, surveyId, studentId, LocalDateTime.now().minusDays(1), null, null));
        }

        @Override
        public List<SurveyRecipient> loadBySurveyId(Integer surveyId) {
            return List.of(new SurveyRecipient(1, surveyId, 3, LocalDateTime.now().minusDays(1), null, null));
        }

        @Override
        public List<SurveyRecipient> loadByStudentId(Integer studentId) {
            return List.of(new SurveyRecipient(1, 1, studentId, LocalDateTime.now().minusDays(1), null, null));
        }

        @Override
        public SurveyRecipient save(SurveyRecipient recipient) {
            return recipient;
        }

        @Override
        public List<SurveyRecipient> saveAll(List<SurveyRecipient> recipients) {
            return recipients;
        }
    }
}
