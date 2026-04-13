package com.ttcs.backend.application.domain.service;

import com.ttcs.backend.application.domain.model.Department;
import com.ttcs.backend.application.domain.model.Question;
import com.ttcs.backend.application.domain.model.QuestionType;
import com.ttcs.backend.application.domain.model.ResponseDetail;
import com.ttcs.backend.application.domain.model.Role;
import com.ttcs.backend.application.domain.model.Status;
import com.ttcs.backend.application.domain.model.Student;
import com.ttcs.backend.application.domain.model.Survey;
import com.ttcs.backend.application.domain.model.SurveyResponse;
import com.ttcs.backend.application.domain.model.User;
import com.ttcs.backend.application.port.in.command.SubmitSurveyAnswerCommand;
import com.ttcs.backend.application.port.in.command.SubmitSurveyCommand;
import com.ttcs.backend.application.port.in.result.SubmitSurveyResult;
import com.ttcs.backend.application.port.in.result.SubmitSurveyResultCode;
import com.ttcs.backend.application.port.out.LoadQuestionPort;
import com.ttcs.backend.application.port.out.LoadStudentPort;
import com.ttcs.backend.application.port.out.LoadSurveyPort;
import com.ttcs.backend.application.port.out.LoadSurveyResponsePort;
import com.ttcs.backend.application.port.out.SaveResponseDetailPort;
import com.ttcs.backend.application.port.out.SaveSurveyResponsePort;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SubmitSurveyServiceTest {

    @Test
    void shouldRejectClosedSurveyBeforePersistingAnything() {
        RecordingSaveSurveyResponsePort saveSurveyResponsePort = new RecordingSaveSurveyResponsePort();
        RecordingSaveResponseDetailPort saveResponseDetailPort = new RecordingSaveResponseDetailPort();
        SubmitSurveyService service = new SubmitSurveyService(
                surveyPort(closedSurvey()),
                studentPort(student()),
                questionPort(List.of(ratingQuestion())),
                surveyResponsePort(false),
                saveSurveyResponsePort,
                saveResponseDetailPort
        );

        SubmitSurveyResult result = service.submitSurvey(new SubmitSurveyCommand(
                1,
                6,
                List.of(new SubmitSurveyAnswerCommand(11, 5, null))
        ));

        assertFalse(result.success());
        assertEquals(SubmitSurveyResultCode.SURVEY_CLOSED, result.code());
        assertEquals(0, saveSurveyResponsePort.saveCalls);
        assertEquals(0, saveResponseDetailPort.saveAllCalls);
    }

    @Test
    void shouldRejectInvalidQuestionWithoutPersistingAnything() {
        RecordingSaveSurveyResponsePort saveSurveyResponsePort = new RecordingSaveSurveyResponsePort();
        RecordingSaveResponseDetailPort saveResponseDetailPort = new RecordingSaveResponseDetailPort();
        SubmitSurveyService service = new SubmitSurveyService(
                surveyPort(openSurvey()),
                studentPort(student()),
                questionPort(List.of(ratingQuestion())),
                surveyResponsePort(false),
                saveSurveyResponsePort,
                saveResponseDetailPort
        );

        SubmitSurveyResult result = service.submitSurvey(new SubmitSurveyCommand(
                1,
                6,
                List.of(new SubmitSurveyAnswerCommand(999, 5, null))
        ));

        assertFalse(result.success());
        assertEquals(SubmitSurveyResultCode.INVALID_INPUT, result.code());
        assertEquals(0, saveSurveyResponsePort.saveCalls);
        assertEquals(0, saveResponseDetailPort.saveAllCalls);
    }

    @Test
    void shouldPersistSurveyResponseAndDetailsWhenSubmissionIsValid() {
        RecordingSaveSurveyResponsePort saveSurveyResponsePort = new RecordingSaveSurveyResponsePort();
        RecordingSaveResponseDetailPort saveResponseDetailPort = new RecordingSaveResponseDetailPort();
        SubmitSurveyService service = new SubmitSurveyService(
                surveyPort(openSurvey()),
                studentPort(student()),
                questionPort(List.of(ratingQuestion(), textQuestion())),
                surveyResponsePort(false),
                saveSurveyResponsePort,
                saveResponseDetailPort
        );

        SubmitSurveyResult result = service.submitSurvey(new SubmitSurveyCommand(
                1,
                6,
                List.of(
                        new SubmitSurveyAnswerCommand(11, 4, null),
                        new SubmitSurveyAnswerCommand(12, null, "Useful survey")
                )
        ));

        assertTrue(result.success());
        assertEquals(SubmitSurveyResultCode.SUBMIT_SUCCESS, result.code());
        assertEquals(1, saveSurveyResponsePort.saveCalls);
        assertEquals(1, saveResponseDetailPort.saveAllCalls);
        assertEquals(2, saveResponseDetailPort.lastSavedDetails.size());
    }

    private LoadSurveyPort surveyPort(Survey survey) {
        return new LoadSurveyPort() {
            @Override
            public Optional<Survey> loadById(Integer surveyId) {
                return Optional.ofNullable(survey);
            }

            @Override
            public List<Survey> loadAll() {
                return List.of(survey);
            }
        };
    }

    private LoadStudentPort studentPort(Student student) {
        return studentId -> Optional.ofNullable(student);
    }

    private LoadQuestionPort questionPort(List<Question> questions) {
        return surveyId -> questions;
    }

    private Survey openSurvey() {
        return new Survey(
                1,
                "Teaching Feedback",
                "Spring semester survey",
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                99,
                false
        );
    }

    private Survey closedSurvey() {
        return new Survey(
                1,
                "Teaching Feedback",
                "Spring semester survey",
                LocalDateTime.now().minusDays(5),
                LocalDateTime.now().minusDays(1),
                99,
                false
        );
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

    private Student student() {
        return new Student(
                6,
                new User(6, "student@example.com", "hashed", Role.STUDENT, true),
                "Student MVP",
                "S0006",
                new Department(1, "Computer Science"),
                Status.ACTIVE,
                null,
                null
        );
    }

    private Question ratingQuestion() {
        return new Question(11, 1, "Rate the course", QuestionType.RATING);
    }

    private Question textQuestion() {
        return new Question(12, 1, "Share a comment", QuestionType.TEXT);
    }

    private static final class RecordingSaveSurveyResponsePort implements SaveSurveyResponsePort {
        private int saveCalls;

        @Override
        public SurveyResponse save(SurveyResponse surveyResponse) {
            saveCalls++;
            return new SurveyResponse(
                    101,
                    surveyResponse.getStudent(),
                    surveyResponse.getTeacher(),
                    surveyResponse.getSurvey(),
                    surveyResponse.getSubmittedAt()
            );
        }
    }

    private static final class RecordingSaveResponseDetailPort implements SaveResponseDetailPort {
        private int saveAllCalls;
        private List<ResponseDetail> lastSavedDetails = new ArrayList<>();

        @Override
        public ResponseDetail save(ResponseDetail responseDetail) {
            return responseDetail;
        }

        @Override
        public List<ResponseDetail> saveAll(List<ResponseDetail> responseDetails) {
            saveAllCalls++;
            lastSavedDetails = responseDetails;
            return responseDetails;
        }
    }
}
