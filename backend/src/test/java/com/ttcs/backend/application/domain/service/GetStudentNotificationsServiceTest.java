package com.ttcs.backend.application.domain.service;

import com.ttcs.backend.application.domain.model.Survey;
import com.ttcs.backend.application.domain.model.SurveyLifecycleState;
import com.ttcs.backend.application.domain.model.SurveyRecipient;
import com.ttcs.backend.application.port.in.resultview.StudentNotificationResult;
import com.ttcs.backend.application.port.out.LoadSurveyPort;
import com.ttcs.backend.application.port.out.LoadSurveyRecipientPort;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GetStudentNotificationsServiceTest {

    @Test
    void shouldCreateNewAndClosingSoonNotificationsForOpenUnsubmittedSurvey() {
        Survey survey = new Survey(
                1,
                "Midterm Feedback",
                "Provide feedback",
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusHours(12),
                1,
                false,
                SurveyLifecycleState.PUBLISHED
        );
        GetStudentNotificationsService service = new GetStudentNotificationsService(
                surveyPort(List.of(survey)),
                recipientPort(new SurveyRecipient(1, 1, 10, LocalDateTime.now().minusDays(1), null, null))
        );

        List<StudentNotificationResult> results = service.getNotifications(10);

        assertEquals(2, results.size());
        assertTrue(results.stream().anyMatch(item -> item.type().equals("NEW_SURVEY")));
        assertTrue(results.stream().anyMatch(item -> item.type().equals("CLOSING_SOON")));
    }

    @Test
    void shouldSkipActionableNotificationsForSubmittedSurvey() {
        Survey survey = new Survey(
                1,
                "Midterm Feedback",
                "Provide feedback",
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusHours(12),
                1,
                false,
                SurveyLifecycleState.PUBLISHED
        );
        GetStudentNotificationsService service = new GetStudentNotificationsService(
                surveyPort(List.of(survey)),
                recipientPort(new SurveyRecipient(1, 1, 10, LocalDateTime.now().minusDays(1), LocalDateTime.now().minusHours(6), LocalDateTime.now().minusHours(1)))
        );

        List<StudentNotificationResult> results = service.getNotifications(10);

        assertTrue(results.isEmpty());
    }

    @Test
    void shouldCreateOpeningSoonNotificationForUpcomingSurvey() {
        Survey survey = new Survey(
                2,
                "Final Feedback",
                "Upcoming survey",
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().plusDays(6),
                1,
                false,
                SurveyLifecycleState.PUBLISHED
        );
        GetStudentNotificationsService service = new GetStudentNotificationsService(
                surveyPort(List.of(survey)),
                recipientPort(new SurveyRecipient(1, 2, 10, LocalDateTime.now().minusDays(1), null, null))
        );

        List<StudentNotificationResult> results = service.getNotifications(10);

        assertEquals(1, results.size());
        assertEquals("OPENING_SOON", results.getFirst().type());
    }

    private LoadSurveyPort surveyPort(List<Survey> surveys) {
        return new LoadSurveyPort() {
            @Override
            public Optional<Survey> loadById(Integer surveyId) {
                return surveys.stream().filter(survey -> survey.getId().equals(surveyId)).findFirst();
            }

            @Override
            public List<Survey> loadAll() {
                return surveys;
            }
        };
    }

    private LoadSurveyRecipientPort recipientPort(SurveyRecipient recipient) {
        return new LoadSurveyRecipientPort() {
            @Override
            public Optional<SurveyRecipient> loadBySurveyIdAndStudentId(Integer surveyId, Integer studentId) {
                return Optional.ofNullable(recipient);
            }

            @Override
            public List<SurveyRecipient> loadBySurveyId(Integer surveyId) {
                return recipient == null ? List.of() : List.of(recipient);
            }

            @Override
            public List<SurveyRecipient> loadByStudentId(Integer studentId) {
                return recipient == null ? List.of() : List.of(recipient);
            }
        };
    }
}
