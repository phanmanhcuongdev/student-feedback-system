package com.ttcs.backend.application.domain.service;

import com.ttcs.backend.application.domain.exception.SurveyNotFoundException;
import com.ttcs.backend.application.domain.model.Department;
import com.ttcs.backend.application.domain.model.EvaluatorType;
import com.ttcs.backend.application.domain.model.Role;
import com.ttcs.backend.application.domain.model.SubjectType;
import com.ttcs.backend.application.domain.model.Survey;
import com.ttcs.backend.application.domain.model.SurveyAssignment;
import com.ttcs.backend.application.domain.model.SurveyLifecycleState;
import com.ttcs.backend.application.domain.model.Teacher;
import com.ttcs.backend.application.domain.model.User;
import com.ttcs.backend.application.port.in.resultview.SurveyResultDetailResult;
import com.ttcs.backend.application.port.in.resultview.SurveyResultSummaryResult;
import com.ttcs.backend.application.port.out.LoadSurveyAssignmentPort;
import com.ttcs.backend.application.port.out.LoadSurveyResultPort;
import com.ttcs.backend.application.port.out.LoadTeacherByUserIdPort;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GetSurveyResultServiceTest {

    @Test
    void shouldAllowAdminToViewAllSurveyResults() {
        GetSurveyResultService service = service();

        List<SurveyResultSummaryResult> results = service.getSurveyResults(99, Role.ADMIN);

        assertEquals(3, results.size());
    }

    @Test
    void shouldRestrictTeacherListToOwnDepartmentSurveys() {
        GetSurveyResultService service = service();

        List<SurveyResultSummaryResult> results = service.getSurveyResults(10, Role.TEACHER);

        assertEquals(1, results.size());
        assertEquals(1, results.getFirst().id());
    }

    @Test
    void shouldDenyTeacherDetailOutsideDepartmentScope() {
        GetSurveyResultService service = service();

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.getSurveyResult(2, 10, Role.TEACHER)
        );

        assertEquals(403, exception.getStatusCode().value());
    }

    @Test
    void shouldDenyTeacherDetailForAllStudentsSurvey() {
        GetSurveyResultService service = service();

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.getSurveyResult(3, 10, Role.TEACHER)
        );

        assertEquals(403, exception.getStatusCode().value());
    }

    @Test
    void shouldAllowTeacherDetailWithinDepartmentScope() {
        GetSurveyResultService service = service();

        SurveyResultDetailResult result = service.getSurveyResult(1, 10, Role.TEACHER);

        assertEquals(1, result.id());
    }

    @Test
    void shouldRejectMissingTeacherProfile() {
        GetSurveyResultService service = new GetSurveyResultService(
                new InMemorySurveyResultPort(),
                new InMemoryAssignmentPort(),
                userId -> Optional.empty()
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.getSurveyResults(10, Role.TEACHER)
        );

        assertEquals(403, exception.getStatusCode().value());
    }

    @Test
    void shouldRejectTeacherWithoutDepartmentScope() {
        GetSurveyResultService service = new GetSurveyResultService(
                new InMemorySurveyResultPort(),
                new InMemoryAssignmentPort(),
                userId -> Optional.of(new Teacher(
                        userId,
                        new User(userId, "teacher@university.edu", "hashed", Role.TEACHER, true),
                        "Teacher Demo",
                        "T0001",
                        new Department(null, "Unknown")
                ))
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.getSurveyResults(10, Role.TEACHER)
        );

        assertEquals(403, exception.getStatusCode().value());
    }

    @Test
    void shouldAllowTeacherWhenAnyDepartmentAssignmentMatches() {
        GetSurveyResultService service = new GetSurveyResultService(
                new InMemorySurveyResultPort(),
                new MultiDepartmentAssignmentPort(),
                userId -> Optional.of(new Teacher(
                        userId,
                        new User(userId, "teacher@university.edu", "hashed", Role.TEACHER, true),
                        "Teacher Demo",
                        "T0001",
                        new Department(1, "Computer Science")
                ))
        );

        SurveyResultDetailResult result = service.getSurveyResult(2, 10, Role.TEACHER);

        assertEquals(2, result.id());
    }

    @Test
    void shouldReturnNotFoundForMissingSurveyEvenForTeacher() {
        GetSurveyResultService service = service();

        assertThrows(SurveyNotFoundException.class, () -> service.getSurveyResult(999, 10, Role.TEACHER));
    }

    @Test
    void shouldStillReturnNotFoundForMissingSurvey() {
        GetSurveyResultService service = service();

        assertThrows(SurveyNotFoundException.class, () -> service.getSurveyResult(999, 99, Role.ADMIN));
    }

    private GetSurveyResultService service() {
        return new GetSurveyResultService(
                new InMemorySurveyResultPort(),
                new InMemoryAssignmentPort(),
                userId -> Optional.of(new Teacher(
                        userId,
                        new User(userId, "teacher@university.edu", "hashed", Role.TEACHER, true),
                        "Teacher Demo",
                        "T0001",
                        new Department(1, "Computer Science")
                ))
        );
    }

    private static final class InMemorySurveyResultPort implements LoadSurveyResultPort {
        private final List<SurveyResultSummaryResult> summaries = List.of(
                summary(1, "Dept 1 Survey"),
                summary(2, "Dept 2 Survey"),
                summary(3, "All Students Survey")
        );

        @Override
        public List<SurveyResultSummaryResult> loadSurveyResults() {
            return summaries;
        }

        @Override
        public Optional<SurveyResultDetailResult> loadSurveyResult(Integer surveyId) {
            return summaries.stream()
                    .filter(item -> item.id().equals(surveyId))
                    .findFirst()
                    .map(item -> new SurveyResultDetailResult(
                            item.id(),
                            item.title(),
                            item.description(),
                            item.startDate(),
                            item.endDate(),
                            item.status(),
                            item.responseCount(),
                            item.targetedCount(),
                            item.openedCount(),
                            item.submittedCount(),
                            item.responseRate(),
                            List.of()
                    ));
        }

        private static SurveyResultSummaryResult summary(Integer id, String title) {
            return new SurveyResultSummaryResult(
                    id,
                    title,
                    "Description",
                    LocalDateTime.now().minusDays(5),
                    LocalDateTime.now().minusDays(1),
                    "CLOSED",
                    12L,
                    20L,
                    15L,
                    12L,
                    60.0
            );
        }
    }

    private static final class InMemoryAssignmentPort implements LoadSurveyAssignmentPort {
        private final Map<Integer, List<SurveyAssignment>> assignments = Map.of(
                1, List.of(departmentAssignment(1, 1)),
                2, List.of(departmentAssignment(2, 2)),
                3, List.of(allStudentsAssignment(3))
        );

        @Override
        public List<SurveyAssignment> loadBySurveyId(Integer surveyId) {
            return assignments.getOrDefault(surveyId, List.of());
        }

        private static SurveyAssignment departmentAssignment(Integer surveyId, Integer departmentId) {
            return new SurveyAssignment(
                    surveyId,
                    new Survey(surveyId, "Survey " + surveyId, "Description", LocalDateTime.now(), LocalDateTime.now().plusDays(1), 1, false, SurveyLifecycleState.PUBLISHED),
                    EvaluatorType.STUDENT,
                    null,
                    SubjectType.DEPARTMENT,
                    departmentId
            );
        }

        private static SurveyAssignment allStudentsAssignment(Integer surveyId) {
            return new SurveyAssignment(
                    surveyId,
                    new Survey(surveyId, "Survey " + surveyId, "Description", LocalDateTime.now(), LocalDateTime.now().plusDays(1), 1, false, SurveyLifecycleState.PUBLISHED),
                    EvaluatorType.STUDENT,
                    null,
                    SubjectType.ALL,
                    null
            );
        }
    }

    private static final class MultiDepartmentAssignmentPort implements LoadSurveyAssignmentPort {
        @Override
        public List<SurveyAssignment> loadBySurveyId(Integer surveyId) {
            if (surveyId.equals(2)) {
                return List.of(
                        InMemoryAssignmentPort.departmentAssignment(2, 2),
                        InMemoryAssignmentPort.departmentAssignment(2, 1)
                );
            }
            return new InMemoryAssignmentPort().loadBySurveyId(surveyId);
        }
    }
}
