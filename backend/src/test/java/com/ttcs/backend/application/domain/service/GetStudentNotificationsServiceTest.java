package com.ttcs.backend.application.domain.service;

import com.ttcs.backend.application.domain.model.Survey;
import com.ttcs.backend.application.port.in.resultview.StudentNotificationResult;
import com.ttcs.backend.application.port.out.LoadSurveyPort;
import com.ttcs.backend.application.port.out.LoadSurveyResponsePort;
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
                false
        );
        GetStudentNotificationsService service = new GetStudentNotificationsService(
                surveyPort(List.of(survey)),
                surveyResponsePort(false)
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
                false
        );
        GetStudentNotificationsService service = new GetStudentNotificationsService(
                surveyPort(List.of(survey)),
                surveyResponsePort(true)
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
                false
        );
        GetStudentNotificationsService service = new GetStudentNotificationsService(
                surveyPort(List.of(survey)),
                surveyResponsePort(false)
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

    private LoadSurveyResponsePort surveyResponsePort(boolean submitted) {
        return new LoadSurveyResponsePort() {
            @Override
            public boolean existsBySurveyIdAndStudentId(Integer surveyId, Integer studentId) {
                return submitted;
            }

            @Override
            public long countBySurveyId(Integer surveyId) {
                return 0;
            }
        };
    }
}
