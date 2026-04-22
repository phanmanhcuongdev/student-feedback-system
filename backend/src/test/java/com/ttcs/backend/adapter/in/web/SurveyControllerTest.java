package com.ttcs.backend.adapter.in.web;

import com.ttcs.backend.adapter.in.web.dto.SubmitSurveyAnswerRequest;
import com.ttcs.backend.adapter.in.web.dto.SubmitSurveyRequest;
import com.ttcs.backend.application.domain.model.Department;
import com.ttcs.backend.application.domain.model.QuestionType;
import com.ttcs.backend.application.domain.model.Role;
import com.ttcs.backend.application.domain.model.Status;
import com.ttcs.backend.application.domain.model.Student;
import com.ttcs.backend.application.domain.model.SurveyStatus;
import com.ttcs.backend.application.domain.model.User;
import com.ttcs.backend.application.port.in.GetStudentSurveysQuery;
import com.ttcs.backend.application.port.in.GetSurveyDetailUseCase;
import com.ttcs.backend.application.port.in.GetSurveyUseCase;
import com.ttcs.backend.application.port.in.SubmitSurveyUseCase;
import com.ttcs.backend.application.port.in.command.SubmitSurveyCommand;
import com.ttcs.backend.application.port.in.result.QuestionItemResult;
import com.ttcs.backend.application.port.in.result.StudentSurveyPageResult;
import com.ttcs.backend.application.port.in.result.SubmitSurveyResult;
import com.ttcs.backend.application.port.in.result.SurveyDetailResult;
import com.ttcs.backend.application.port.in.result.SurveySummaryResult;
import com.ttcs.backend.application.port.out.auth.LoadStudentByIdPort;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SurveyControllerTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void submitSurveyShouldCallOnlySubmitUseCase() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                6,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_STUDENT"))
        ));
        ThrowingGetSurveyUseCase getSurveyUseCase = new ThrowingGetSurveyUseCase();
        RecordingSubmitSurveyUseCase submitSurveyUseCase = new RecordingSubmitSurveyUseCase();
        SurveyController controller = new SurveyController(
                getSurveyUseCase,
                new StubGetSurveyDetailUseCase(),
                submitSurveyUseCase,
                new CurrentIdentityProvider(new StubLoadStudentByIdPort())
        );

        controller.submitSurvey(1, new SubmitSurveyRequest(List.of(new SubmitSurveyAnswerRequest(11, 5, null))));

        assertEquals(0, getSurveyUseCase.getSurveyByIdCalls);
        assertEquals(1, submitSurveyUseCase.calls);
        assertEquals(1, submitSurveyUseCase.lastCommand.surveyId());
        assertEquals(42, submitSurveyUseCase.lastCommand.studentId());
        assertEquals(1, submitSurveyUseCase.lastCommand.answers().size());
    }

    private static final class ThrowingGetSurveyUseCase implements GetSurveyUseCase {
        private int getSurveyByIdCalls;

        @Override
        public SurveySummaryResult getSurveyById(Integer surveyId, Integer studentUserId) {
            getSurveyByIdCalls++;
            throw new AssertionError("submit flow must not call read survey use case");
        }

        @Override
        public StudentSurveyPageResult getAllSurveys(GetStudentSurveysQuery query, Integer studentUserId) {
            throw new AssertionError("not used");
        }
    }

    private static final class StubGetSurveyDetailUseCase implements GetSurveyDetailUseCase {
        @Override
        public SurveyDetailResult getSurveyDetail(Integer surveyId, Integer studentId, String targetLang) {
            return new SurveyDetailResult(
                    surveyId,
                    "Survey",
                    "Description",
                    LocalDateTime.now().minusDays(1),
                    LocalDateTime.now().plusDays(1),
                    SurveyStatus.OPEN,
                    List.of(new QuestionItemResult(11, "Rate", QuestionType.RATING))
            );
        }
    }

    private static final class RecordingSubmitSurveyUseCase implements SubmitSurveyUseCase {
        private int calls;
        private SubmitSurveyCommand lastCommand;

        @Override
        public SubmitSurveyResult submitSurvey(SubmitSurveyCommand command) {
            calls++;
            lastCommand = command;
            return SubmitSurveyResult.success("ok");
        }
    }

    private static final class StubLoadStudentByIdPort implements LoadStudentByIdPort {
        @Override
        public Optional<Student> loadById(Integer studentId) {
            return Optional.of(student(studentId));
        }

        @Override
        public Optional<Student> loadByUserId(Integer userId) {
            return Optional.of(student(42, userId));
        }

        private Student student(Integer id) {
            return student(id, id);
        }

        private Student student(Integer id, Integer userId) {
            return new Student(
                    id,
                    new User(userId, "student@example.com", "hashed", Role.STUDENT, true),
                    "Student",
                    "S0006",
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
    }
}
