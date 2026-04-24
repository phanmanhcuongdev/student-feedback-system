package com.ttcs.backend.application.domain.service;

import com.ttcs.backend.application.domain.exception.SurveyNotFoundException;
import com.ttcs.backend.application.domain.model.Department;
import com.ttcs.backend.application.domain.model.Role;
import com.ttcs.backend.application.domain.model.Status;
import com.ttcs.backend.application.domain.model.Student;
import com.ttcs.backend.application.domain.model.Survey;
import com.ttcs.backend.application.domain.model.SurveyLifecycleState;
import com.ttcs.backend.application.domain.model.SurveyRecipient;
import com.ttcs.backend.application.domain.model.SurveyStatus;
import com.ttcs.backend.application.domain.model.User;
import com.ttcs.backend.application.port.out.LoadStudentSurveysQuery;
import com.ttcs.backend.application.port.out.LoadSurveyPort;
import com.ttcs.backend.application.port.out.LoadSurveyRecipientPort;
import com.ttcs.backend.application.port.out.SaveSurveyRecipientPort;
import com.ttcs.backend.application.port.out.StudentSurveySearchPage;
import com.ttcs.backend.application.port.out.auth.LoadStudentByIdPort;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GetSurveyServiceTest {

    @Test
    void shouldRejectWhenRecipientRecordDoesNotExist() {
        GetSurveyService service = new GetSurveyService(
                surveyPort(survey()),
                studentPort(),
                new EmptyRecipientPort(),
                new RecordingRecipientPort()
        );

        assertThrows(SurveyNotFoundException.class, () -> service.getSurveyById(1, 3, "en"));
    }

    @Test
    void shouldMarkRecipientOpenedOnFirstDirectAccess() {
        RecordingRecipientPort recipientPort = new RecordingRecipientPort();
        GetSurveyService service = new GetSurveyService(
                surveyPort(survey()),
                studentPort(),
                recipientPort,
                recipientPort
        );

        var result = service.getSurveyById(1, 3, "en");

        assertEquals(1, result.id());
        assertEquals(1, recipientPort.saveCalls);
        assertFalse(result.submitted());
    }

    @Test
    void shouldNotRewriteOpenedAtAfterFirstOpen() {
        RecordingRecipientPort recipientPort = new RecordingRecipientPort(true);
        GetSurveyService service = new GetSurveyService(
                surveyPort(survey()),
                studentPort(),
                recipientPort,
                recipientPort
        );

        service.getSurveyById(1, 3, "en");

        assertEquals(0, recipientPort.saveCalls);
    }

    @Test
    void shouldExposeSubmittedFlagInSurveySummary() {
        RecordingRecipientPort recipientPort = new RecordingRecipientPort(true, true);
        GetSurveyService service = new GetSurveyService(
                surveyPort(survey()),
                studentPort(),
                recipientPort,
                recipientPort
        );

        var result = service.getSurveyById(1, 3, "en");

        assertTrue(result.submitted());
    }

    @Test
    void shouldReturnTranslatedSurveyMetadataWhenAvailable() {
        GetSurveyService service = new GetSurveyService(
                surveyPort(translatedSurvey()),
                studentPort(),
                new RecordingRecipientPort(),
                new RecordingRecipientPort()
        );

        var result = service.getSurveyById(1, 3, "en");

        assertEquals("Published Survey EN", result.title());
        assertEquals("Description EN", result.description());
    }

    @Test
    void shouldRejectClosedSurveyEvenWhenRecipientExists() {
        GetSurveyService service = new GetSurveyService(
                surveyPort(new Survey(1, "Closed Survey", "Desc", LocalDateTime.now().minusDays(3), LocalDateTime.now().minusMinutes(1), 1, false, SurveyLifecycleState.PUBLISHED)),
                studentPort(),
                new RecordingRecipientPort(),
                new RecordingRecipientPort()
        );

        assertThrows(SurveyNotFoundException.class, () -> service.getSurveyById(1, 3, "en"));
    }

    private LoadSurveyPort surveyPort(Survey survey) {
        return new LoadSurveyPort() {
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
                assertEquals("en", query.targetLang());
                return new StudentSurveySearchPage(List.of(), 0, 0, 0, 0);
            }
        };
    }

    private LoadStudentByIdPort studentPort() {
        return new LoadStudentByIdPort() {
            @Override
            public Optional<Student> loadById(Integer studentId) {
                return Optional.of(student(studentId));
            }

            @Override
            public Optional<Student> loadByUserId(Integer userId) {
                return Optional.of(student(userId));
            }
        };
    }

    private Student student(Integer userId) {
        return new Student(
                userId,
                new User(userId, "student@example.com", "pw", Role.STUDENT, true),
                "Student",
                "S0001",
                new Department(1, "Computer Science"),
                Status.ACTIVE,
                null,
                null,
                null,
                null,
                null,
                null,
                0
        );
    }

    private Survey survey() {
        return new Survey(1, "Published Survey", "Desc", LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1), 1, false, SurveyLifecycleState.PUBLISHED);
    }

    private Survey translatedSurvey() {
        return new Survey(
                1,
                "Published Survey",
                "Khao sat da phat hanh",
                "Published Survey EN",
                "Desc",
                "Mo ta",
                "Description EN",
                "vi",
                true,
                "test-model",
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                1,
                false,
                SurveyLifecycleState.PUBLISHED
        );
    }

    private static final class EmptyRecipientPort implements LoadSurveyRecipientPort {
        @Override
        public Optional<SurveyRecipient> loadBySurveyIdAndStudentId(Integer surveyId, Integer studentId) {
            return Optional.empty();
        }

        @Override
        public List<SurveyRecipient> loadBySurveyId(Integer surveyId) {
            return List.of();
        }

        @Override
        public List<SurveyRecipient> loadByStudentId(Integer studentId) {
            return List.of();
        }
    }

    private static final class RecordingRecipientPort implements LoadSurveyRecipientPort, SaveSurveyRecipientPort {
        private int saveCalls;
        private final boolean alreadyOpened;
        private final boolean alreadySubmitted;

        private RecordingRecipientPort() {
            this(false, false);
        }

        private RecordingRecipientPort(boolean alreadyOpened) {
            this(alreadyOpened, false);
        }

        private RecordingRecipientPort(boolean alreadyOpened, boolean alreadySubmitted) {
            this.alreadyOpened = alreadyOpened;
            this.alreadySubmitted = alreadySubmitted;
        }

        @Override
        public Optional<SurveyRecipient> loadBySurveyIdAndStudentId(Integer surveyId, Integer studentId) {
            return Optional.of(new SurveyRecipient(
                    1,
                    surveyId,
                    studentId,
                    LocalDateTime.now().minusDays(1),
                    alreadyOpened ? LocalDateTime.now().minusHours(2) : null,
                    alreadySubmitted ? LocalDateTime.now().minusHours(1) : null
            ));
        }

        @Override
        public List<SurveyRecipient> loadBySurveyId(Integer surveyId) {
            return List.of();
        }

        @Override
        public List<SurveyRecipient> loadByStudentId(Integer studentId) {
            return List.of(new SurveyRecipient(
                    1,
                    1,
                    studentId,
                    LocalDateTime.now().minusDays(1),
                    alreadyOpened ? LocalDateTime.now().minusHours(2) : null,
                    alreadySubmitted ? LocalDateTime.now().minusHours(1) : null
            ));
        }

        @Override
        public SurveyRecipient save(SurveyRecipient recipient) {
            saveCalls++;
            return recipient;
        }

        @Override
        public List<SurveyRecipient> saveAll(List<SurveyRecipient> recipients) {
            return recipients;
        }
    }
}
