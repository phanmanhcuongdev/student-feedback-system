package com.ttcs.backend.application.domain.service;

import com.ttcs.backend.application.domain.exception.SurveyNotFoundException;
import com.ttcs.backend.application.domain.model.Department;
import com.ttcs.backend.application.domain.model.EvaluatorType;
import com.ttcs.backend.application.domain.model.Role;
import com.ttcs.backend.application.domain.model.SubjectType;
import com.ttcs.backend.application.domain.model.Survey;
import com.ttcs.backend.application.domain.model.SurveyAssignment;
import com.ttcs.backend.application.domain.model.SurveyLifecycleState;
import com.ttcs.backend.application.domain.model.Lecturer;
import com.ttcs.backend.application.domain.model.User;
import com.ttcs.backend.application.port.in.resultview.SurveyResultDetailResult;
import com.ttcs.backend.application.port.in.resultview.GetSurveyResultsQuery;
import com.ttcs.backend.application.port.in.resultview.SurveyResultSummaryResult;
import com.ttcs.backend.application.port.out.LoadSurveyAssignmentPort;
import com.ttcs.backend.application.port.out.LoadSurveyResultPort;
import com.ttcs.backend.application.port.out.LoadSurveyResultsQuery;
import com.ttcs.backend.application.port.out.LoadLecturerByUserIdPort;
import com.ttcs.backend.application.port.out.QuestionStatistics;
import com.ttcs.backend.application.port.out.RatingBreakdown;
import com.ttcs.backend.application.port.out.SurveyResultDetail;
import com.ttcs.backend.application.port.out.SurveyResultMetrics;
import com.ttcs.backend.application.port.out.SurveyResultSearchItem;
import com.ttcs.backend.application.port.out.SurveyResultSearchPage;
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

        List<SurveyResultSummaryResult> results = service.getSurveyResults(defaultQuery(), 99, Role.ADMIN, "vi").items();

        assertEquals(3, results.size());
    }

    @Test
    void shouldRestrictLecturerListToOwnDepartmentSurveys() {
        GetSurveyResultService service = service();

        List<SurveyResultSummaryResult> results = service.getSurveyResults(defaultQuery(), 10, Role.LECTURER, "vi").items();

        assertEquals(1, results.size());
        assertEquals(1, results.getFirst().id());
    }

    @Test
    void shouldDenyLecturerDetailOutsideDepartmentScope() {
        GetSurveyResultService service = service();

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.getSurveyResult(2, 10, Role.LECTURER, "vi")
        );

        assertEquals(403, exception.getStatusCode().value());
    }

    @Test
    void shouldDenyLecturerDetailForAllStudentsSurvey() {
        GetSurveyResultService service = service();

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.getSurveyResult(3, 10, Role.LECTURER, "vi")
        );

        assertEquals(403, exception.getStatusCode().value());
    }

    @Test
    void shouldAllowLecturerDetailWithinDepartmentScope() {
        GetSurveyResultService service = service();

        SurveyResultDetailResult result = service.getSurveyResult(1, 10, Role.LECTURER, "vi");

        assertEquals(1, result.id());
    }

    @Test
    void shouldMapOutputDetailModelToInputResultModel() {
        GetSurveyResultService service = service();

        SurveyResultDetailResult result = service.getSurveyResult(1, 99, Role.ADMIN, "vi");

        assertEquals(1, result.questions().size());
        assertEquals(100, result.questions().getFirst().id());
        assertEquals(5, result.questions().getFirst().ratingBreakdown().getFirst().rating());
        assertEquals(7L, result.questions().getFirst().ratingBreakdown().getFirst().count());
    }

    @Test
    void shouldRejectMissingLecturerProfile() {
        GetSurveyResultService service = new GetSurveyResultService(
                new InMemorySurveyResultPort(),
                new InMemoryAssignmentPort(),
                userId -> Optional.empty()
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.getSurveyResults(defaultQuery(), 10, Role.LECTURER, "vi")
        );

        assertEquals(403, exception.getStatusCode().value());
    }

    @Test
    void shouldRejectLecturerWithoutDepartmentScope() {
        GetSurveyResultService service = new GetSurveyResultService(
                new InMemorySurveyResultPort(),
                new InMemoryAssignmentPort(),
                userId -> Optional.of(new Lecturer(
                        userId,
                        new User(userId, "lecturer@university.edu", "hashed", Role.LECTURER, true),
                        "Lecturer Demo",
                        "T0001",
                        new Department(null, "Unknown")
                ))
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.getSurveyResults(defaultQuery(), 10, Role.LECTURER, "vi")
        );

        assertEquals(403, exception.getStatusCode().value());
    }

    @Test
    void shouldAllowLecturerWhenAnyDepartmentAssignmentMatches() {
        GetSurveyResultService service = new GetSurveyResultService(
                new InMemorySurveyResultPort(),
                new MultiDepartmentAssignmentPort(),
                userId -> Optional.of(new Lecturer(
                        userId,
                        new User(userId, "lecturer@university.edu", "hashed", Role.LECTURER, true),
                        "Lecturer Demo",
                        "T0001",
                        new Department(1, "Computer Science")
                ))
        );

        SurveyResultDetailResult result = service.getSurveyResult(2, 10, Role.LECTURER, "vi");

        assertEquals(2, result.id());
    }

    @Test
    void shouldReturnNotFoundForMissingSurveyEvenForLecturer() {
        GetSurveyResultService service = service();

        assertThrows(SurveyNotFoundException.class, () -> service.getSurveyResult(999, 10, Role.LECTURER, "vi"));
    }

    @Test
    void shouldStillReturnNotFoundForMissingSurvey() {
        GetSurveyResultService service = service();

        assertThrows(SurveyNotFoundException.class, () -> service.getSurveyResult(999, 99, Role.ADMIN, "vi"));
    }

    private GetSurveyResultService service() {
        return new GetSurveyResultService(
                new InMemorySurveyResultPort(),
                new InMemoryAssignmentPort(),
                userId -> Optional.of(new Lecturer(
                        userId,
                        new User(userId, "lecturer@university.edu", "hashed", Role.LECTURER, true),
                        "Lecturer Demo",
                        "T0001",
                        new Department(1, "Computer Science")
                ))
        );
    }

    private GetSurveyResultsQuery defaultQuery() {
        return new GetSurveyResultsQuery(null, null, null, null, null, null, 0, 20, "responseRate", "desc");
    }

    private static final class InMemorySurveyResultPort implements LoadSurveyResultPort {
        private final List<SurveyResultSearchItem> summaries = List.of(
                summary(1, "Dept 1 Survey"),
                summary(2, "Dept 2 Survey"),
                summary(3, "All Students Survey")
        );

        @Override
        public SurveyResultSearchPage loadPage(LoadSurveyResultsQuery query, String targetLang) {
            List<SurveyResultSearchItem> filtered = summaries.stream()
                    .filter(item -> {
                        if (query.lecturerDepartmentId() == null) {
                            return true;
                        }
                        return (query.lecturerDepartmentId().equals(1) && item.id().equals(1))
                                || (query.lecturerDepartmentId().equals(2) && item.id().equals(2));
                    })
                    .toList();
            return new SurveyResultSearchPage(
                    filtered,
                    query.page(),
                    query.size(),
                    filtered.size(),
                    filtered.isEmpty() ? 0 : 1,
                    new SurveyResultMetrics(filtered.size(), 0, filtered.size(), 60.0, filtered.size() * 12L, filtered.size() * 12L)
            );
        }

        @Override
        public Optional<SurveyResultDetail> loadSurveyResult(Integer surveyId, String targetLang) {
            return summaries.stream()
                    .filter(item -> item.id().equals(surveyId))
                    .findFirst()
                    .map(item -> new SurveyResultDetail(
                            item.id(),
                            item.title(),
                            item.description(),
                            item.startDate(),
                            item.endDate(),
                            item.status(),
                            item.lifecycleState(),
                            item.runtimeStatus(),
                            item.recipientScope(),
                            item.recipientDepartmentName(),
                            item.responseCount(),
                            item.targetedCount(),
                            item.openedCount(),
                            item.submittedCount(),
                            item.responseRate(),
                            List.of(new QuestionStatistics(
                                    100,
                                    "How useful?",
                                    "RATING",
                                    12L,
                                    4.5,
                                    List.of(new RatingBreakdown(5, 7L)),
                                    List.of()
                            ))
                    ));
        }

        private static SurveyResultSearchItem summary(Integer id, String title) {
            return new SurveyResultSearchItem(
                    id,
                    title,
                    "Description",
                    LocalDateTime.now().minusDays(5),
                    LocalDateTime.now().minusDays(1),
                    "CLOSED",
                    "CLOSED",
                    "CLOSED",
                    title.contains("All Students") ? "ALL_STUDENTS" : "DEPARTMENT",
                    title.contains("Dept 1") ? "Computer Science" : title.contains("Dept 2") ? "Department 2" : null,
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
