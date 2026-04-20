package com.ttcs.backend.application.domain.service;

import com.ttcs.backend.application.domain.model.AuditActionType;
import com.ttcs.backend.application.domain.model.AuditLog;
import com.ttcs.backend.application.domain.model.EvaluatorType;
import com.ttcs.backend.application.domain.model.Question;
import com.ttcs.backend.application.domain.model.QuestionType;
import com.ttcs.backend.application.domain.model.SubjectType;
import com.ttcs.backend.application.domain.model.Survey;
import com.ttcs.backend.application.domain.model.SurveyAssignment;
import com.ttcs.backend.application.domain.model.SurveyLifecycleState;
import com.ttcs.backend.application.domain.model.SurveyRecipient;
import com.ttcs.backend.application.domain.model.SurveyRecipientScope;
import com.ttcs.backend.application.domain.model.Department;
import com.ttcs.backend.application.domain.model.Role;
import com.ttcs.backend.application.domain.model.Status;
import com.ttcs.backend.application.domain.model.Student;
import com.ttcs.backend.application.domain.model.User;
import com.ttcs.backend.application.port.in.admin.SurveyManagementActionResult;
import com.ttcs.backend.application.port.in.admin.UpdateSurveyCommand;
import com.ttcs.backend.application.port.in.admin.UpdateSurveyQuestionCommand;
import com.ttcs.backend.application.port.out.LoadQuestionPort;
import com.ttcs.backend.application.port.out.LoadSurveyRecipientCandidatePort;
import com.ttcs.backend.application.port.out.LoadSurveyRecipientPort;
import com.ttcs.backend.application.port.out.LoadSurveyAssignmentPort;
import com.ttcs.backend.application.port.out.LoadSurveyPort;
import com.ttcs.backend.application.port.out.LoadSurveyResponsePort;
import com.ttcs.backend.application.port.out.LoadStudentSurveysQuery;
import com.ttcs.backend.application.port.out.LoadStudentPort;
import com.ttcs.backend.application.port.out.NotificationCreateCommand;
import com.ttcs.backend.application.port.out.SaveQuestionPort;
import com.ttcs.backend.application.port.out.SaveAuditLogPort;
import com.ttcs.backend.application.port.out.SaveSurveyRecipientPort;
import com.ttcs.backend.application.port.out.SaveSurveyAssignmentPort;
import com.ttcs.backend.application.port.out.SaveSurveyPort;
import com.ttcs.backend.application.port.out.admin.ManageSurveyPort;
import com.ttcs.backend.application.port.out.admin.ManagedSurveyMetrics;
import com.ttcs.backend.application.port.out.admin.ManagedSurveySearchItem;
import com.ttcs.backend.application.port.out.admin.ManagedSurveySearchPage;
import com.ttcs.backend.application.port.out.admin.ManageSurveysQuery;
import com.ttcs.backend.application.port.out.StudentSurveySearchItem;
import com.ttcs.backend.application.port.out.StudentSurveySearchPage;
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
    void shouldUpdateDraftSurveyWhenNoResponsesExist() {
        InMemorySurveyState state = new InMemorySurveyState(0, SurveyLifecycleState.DRAFT);
        AdminSurveyManagementService service = service(state);

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
        assertEquals(SurveyLifecycleState.DRAFT, state.survey.getLifecycleState());
        assertEquals(1, state.questions.size());
        assertEquals(2, state.assignments.getFirst().getSubjectValue());
    }

    @Test
    void shouldRejectUpdateWhenSurveyIsPublished() {
        InMemorySurveyState state = new InMemorySurveyState(0, SurveyLifecycleState.PUBLISHED);
        AdminSurveyManagementService service = service(state);

        SurveyManagementActionResult result = service.updateSurvey(new UpdateSurveyCommand(
                1,
                "Updated Survey",
                "Updated description",
                state.survey.getStartDate(),
                state.survey.getEndDate(),
                List.of(new UpdateSurveyQuestionCommand("Updated question", "TEXT")),
                SurveyRecipientScope.ALL_STUDENTS,
                null
        ));

        assertFalse(result.success());
        assertEquals("SURVEY_NOT_EDITABLE", result.code());
    }

    @Test
    void shouldPublishDraftSurveyWhenItIsReady() {
        InMemorySurveyState state = new InMemorySurveyState(0, SurveyLifecycleState.DRAFT);
        AdminSurveyManagementService service = service(state);

        SurveyManagementActionResult result = service.publishSurvey(1, 1);

        assertTrue(result.success());
        assertEquals("SURVEY_PUBLISHED", result.code());
        assertEquals(SurveyLifecycleState.PUBLISHED, state.survey.getLifecycleState());
        assertEquals(2, state.recipients.size());
        assertEquals(1, state.auditLogs.size());
        assertEquals(AuditActionType.SURVEY_PUBLISHED, state.auditLogs.getFirst().getActionType());
        assertEquals("SURVEY_PUBLISHED", state.notifications.getFirst().type());
        assertEquals(List.of(103, 104), state.notifications.getFirst().recipientUserIds());
    }

    @Test
    void shouldCreateDeadlineReminderWhenPublishedSurveyIsAlreadyClosingSoon() {
        InMemorySurveyState state = new InMemorySurveyState(0, SurveyLifecycleState.DRAFT);
        state.survey = new Survey(
                1,
                "Closing Survey",
                "Initial description",
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now().plusHours(12),
                1,
                false,
                SurveyLifecycleState.DRAFT
        );
        AdminSurveyManagementService service = service(state);

        SurveyManagementActionResult result = service.publishSurvey(1, 1);

        assertTrue(result.success());
        assertEquals(2, state.notifications.size());
        assertEquals("SURVEY_PUBLISHED", state.notifications.get(0).type());
        assertEquals("SURVEY_DEADLINE_REMINDER", state.notifications.get(1).type());
        assertEquals(List.of(103, 104), state.notifications.get(1).recipientUserIds());
    }

    @Test
    void shouldNotDuplicateRecipientsIfSomeAlreadyExist() {
        InMemorySurveyState state = new InMemorySurveyState(0, SurveyLifecycleState.DRAFT);
        state.recipients.add(new SurveyRecipient(1, 1, 3, LocalDateTime.now().minusHours(1), null, null));
        AdminSurveyManagementService service = service(state);

        SurveyManagementActionResult result = service.publishSurvey(1, 1);

        assertTrue(result.success());
        assertEquals(2, state.recipients.size());
    }

    @Test
    void shouldRejectPublishWhenDraftHasNoValidWindow() {
        InMemorySurveyState state = new InMemorySurveyState(0, SurveyLifecycleState.DRAFT);
        state.survey = new Survey(1, "Initial Survey", "Initial description", null, null, 1, false, SurveyLifecycleState.DRAFT);
        AdminSurveyManagementService service = service(state);

        SurveyManagementActionResult result = service.publishSurvey(1, 1);

        assertFalse(result.success());
        assertEquals("SURVEY_NOT_READY", result.code());
        assertTrue(state.auditLogs.isEmpty());
    }

    @Test
    void shouldRejectPublishWhenDraftHasBlankQuestionContent() {
        InMemorySurveyState state = new InMemorySurveyState(0, SurveyLifecycleState.DRAFT);
        state.questions = new ArrayList<>(List.of(
                new Question(1, 1, "   ", QuestionType.TEXT)
        ));
        AdminSurveyManagementService service = service(state);

        SurveyManagementActionResult result = service.publishSurvey(1, 1);

        assertFalse(result.success());
        assertEquals("SURVEY_NOT_READY", result.code());
        assertTrue(state.auditLogs.isEmpty());
    }

    @Test
    void shouldRejectPublishWhenDepartmentAssignmentHasNoDepartmentId() {
        InMemorySurveyState state = new InMemorySurveyState(0, SurveyLifecycleState.DRAFT);
        state.assignments = new ArrayList<>(List.of(
                new SurveyAssignment(1, state.survey, EvaluatorType.STUDENT, null, SubjectType.DEPARTMENT, null)
        ));
        AdminSurveyManagementService service = service(state);

        SurveyManagementActionResult result = service.publishSurvey(1, 1);

        assertFalse(result.success());
        assertEquals("SURVEY_NOT_READY", result.code());
        assertTrue(state.auditLogs.isEmpty());
    }

    @Test
    void shouldRejectUpdateWhenDepartmentScopeHasNoDepartmentId() {
        InMemorySurveyState state = new InMemorySurveyState(0, SurveyLifecycleState.DRAFT);
        AdminSurveyManagementService service = service(state);

        SurveyManagementActionResult result = service.updateSurvey(new UpdateSurveyCommand(
                1,
                "Updated Survey",
                "Updated description",
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(5),
                List.of(new UpdateSurveyQuestionCommand("Updated question", "TEXT")),
                SurveyRecipientScope.DEPARTMENT,
                null
        ));

        assertFalse(result.success());
        assertEquals("INVALID_INPUT", result.code());
    }

    @Test
    void shouldOnlyCreateRecipientsForEligibleActiveVerifiedStudents() {
        InMemorySurveyState state = new InMemorySurveyState(0, SurveyLifecycleState.DRAFT);
        state.candidateStudents = new ArrayList<>(List.of(
                student(3, 1),
                inactiveStudent(9, 1)
        ));
        AdminSurveyManagementService service = service(state);

        SurveyManagementActionResult result = service.publishSurvey(1, 1);

        assertTrue(result.success());
        assertEquals(1, state.recipients.size());
    }

    @Test
    void shouldClosePublishedSurvey() {
        InMemorySurveyState state = new InMemorySurveyState(0, SurveyLifecycleState.PUBLISHED);
        AdminSurveyManagementService service = service(state);

        SurveyManagementActionResult result = service.closeSurvey(1, 1);

        assertTrue(result.success());
        assertEquals(SurveyLifecycleState.CLOSED, state.survey.getLifecycleState());
        assertTrue(state.survey.getEndDate().isBefore(LocalDateTime.now().plusSeconds(1)));
        assertEquals(AuditActionType.SURVEY_CLOSED, state.auditLogs.getFirst().getActionType());
    }

    @Test
    void shouldArchiveClosedSurvey() {
        InMemorySurveyState state = new InMemorySurveyState(0, SurveyLifecycleState.CLOSED);
        AdminSurveyManagementService service = service(state);

        SurveyManagementActionResult result = service.archiveSurvey(1, 1);

        assertTrue(result.success());
        assertEquals("SURVEY_ARCHIVED", result.code());
        assertEquals(SurveyLifecycleState.ARCHIVED, state.survey.getLifecycleState());
        assertEquals(AuditActionType.SURVEY_ARCHIVED, state.auditLogs.getFirst().getActionType());
    }

    @Test
    void shouldRejectArchiveForPublishedSurvey() {
        InMemorySurveyState state = new InMemorySurveyState(0, SurveyLifecycleState.PUBLISHED);
        AdminSurveyManagementService service = service(state);

        SurveyManagementActionResult result = service.archiveSurvey(1, 1);

        assertFalse(result.success());
        assertEquals("INVALID_TRANSITION", result.code());
        assertTrue(state.auditLogs.isEmpty());
    }

    @Test
    void shouldAllowVisibilityToggleOnlyForPublishedOrClosedSurvey() {
        InMemorySurveyState draftState = new InMemorySurveyState(0, SurveyLifecycleState.DRAFT);
        AdminSurveyManagementService draftService = service(draftState);

        SurveyManagementActionResult draftResult = draftService.setHidden(1, true, 1);

        assertFalse(draftResult.success());
        assertEquals("INVALID_TRANSITION", draftResult.code());
        assertTrue(draftState.auditLogs.isEmpty());

        InMemorySurveyState publishedState = new InMemorySurveyState(0, SurveyLifecycleState.PUBLISHED);
        AdminSurveyManagementService publishedService = service(publishedState);

        SurveyManagementActionResult publishedResult = publishedService.setHidden(1, true, 1);

        assertTrue(publishedResult.success());
        assertTrue(publishedState.survey.isHidden());
        assertEquals(AuditActionType.SURVEY_VISIBILITY_CHANGED, publishedState.auditLogs.getFirst().getActionType());
        assertEquals("VISIBLE", publishedState.auditLogs.getFirst().getOldState());
        assertEquals("HIDDEN", publishedState.auditLogs.getFirst().getNewState());
    }

    @Test
    void shouldRejectCloseForDraftSurvey() {
        InMemorySurveyState state = new InMemorySurveyState(0, SurveyLifecycleState.DRAFT);
        AdminSurveyManagementService service = service(state);

        SurveyManagementActionResult result = service.closeSurvey(1, 1);

        assertFalse(result.success());
        assertEquals("INVALID_TRANSITION", result.code());
        assertTrue(state.auditLogs.isEmpty());
    }

    @Test
    void shouldRejectPublishWithoutActorAndSkipAudit() {
        InMemorySurveyState state = new InMemorySurveyState(0, SurveyLifecycleState.DRAFT);
        AdminSurveyManagementService service = service(state);

        SurveyManagementActionResult result = service.publishSurvey(1, null);

        assertFalse(result.success());
        assertEquals("INVALID_INPUT", result.code());
        assertTrue(state.auditLogs.isEmpty());
    }

    private AdminSurveyManagementService service(InMemorySurveyState state) {
        return new AdminSurveyManagementService(
                new SurveyPort(state),
                new SurveyPort(state),
                new QuestionPort(state),
                new QuestionPort(state),
                new AssignmentPort(state),
                new AssignmentPort(state),
                new ResponsePort(state),
                new RecipientPort(state),
                new RecipientPort(state),
                new CandidatePort(state),
                new StudentPort(state),
                new AuditPort(state),
                command -> state.notifications.add(command),
                new ManageSurveyQueryPort(state)
        );
    }

    private static final class InMemorySurveyState {
        private Survey survey;
        private List<Question> questions = new ArrayList<>(List.of(
                new Question(1, 1, "Initial question", QuestionType.RATING)
        ));
        private List<SurveyAssignment> assignments;
        private List<SurveyRecipient> recipients = new ArrayList<>();
        private List<AuditLog> auditLogs = new ArrayList<>();
        private List<NotificationCreateCommand> notifications = new ArrayList<>();
        private List<Student> candidateStudents = new ArrayList<>(List.of(
                student(3, 1),
                student(4, 2)
        ));
        private final long responseCount;

        private InMemorySurveyState(long responseCount, SurveyLifecycleState lifecycleState) {
            this.responseCount = responseCount;
            this.survey = new Survey(
                    1,
                    "Initial Survey",
                    "Initial description",
                    LocalDateTime.now().minusDays(2),
                    LocalDateTime.now().plusDays(2),
                    1,
                    false,
                    lifecycleState
            );
            this.assignments = new ArrayList<>(List.of(
                    new SurveyAssignment(1, survey, EvaluatorType.STUDENT, null, SubjectType.ALL, null)
            ));
        }
    }

    private static Student student(Integer id, Integer departmentId) {
        return new Student(
                id,
                new User(id + 100, "student" + id + "@example.com", "hashed", Role.STUDENT, true),
                "Student " + id,
                "S" + id,
                new Department(departmentId, "Department " + departmentId),
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

    private static Student inactiveStudent(Integer id, Integer departmentId) {
        return new Student(
                id,
                new User(id + 100, "student" + id + "@example.com", "hashed", Role.STUDENT, false),
                "Student " + id,
                "S" + id,
                new Department(departmentId, "Department " + departmentId),
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
        public StudentSurveySearchPage loadStudentSurveyPage(LoadStudentSurveysQuery query) {
            return new StudentSurveySearchPage(
                    List.of(new StudentSurveySearchItem(
                            state.survey.getId(),
                            state.survey.getTitle(),
                            state.survey.getDescription(),
                            state.survey.getStartDate(),
                            state.survey.getEndDate(),
                            state.survey.getCreatedBy(),
                            state.survey.status()
                    )),
                    0,
                    1,
                    1,
                    1
            );
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

    private static final class RecipientPort implements LoadSurveyRecipientPort, SaveSurveyRecipientPort {
        private final InMemorySurveyState state;

        private RecipientPort(InMemorySurveyState state) {
            this.state = state;
        }

        @Override
        public java.util.Optional<SurveyRecipient> loadBySurveyIdAndStudentId(Integer surveyId, Integer studentId) {
            return state.recipients.stream()
                    .filter(item -> item.getSurveyId().equals(surveyId) && item.getStudentId().equals(studentId))
                    .findFirst();
        }

        @Override
        public List<SurveyRecipient> loadBySurveyId(Integer surveyId) {
            return state.recipients.stream().filter(item -> item.getSurveyId().equals(surveyId)).toList();
        }

        @Override
        public List<SurveyRecipient> loadByStudentId(Integer studentId) {
            return state.recipients.stream().filter(item -> item.getStudentId().equals(studentId)).toList();
        }

        @Override
        public SurveyRecipient save(SurveyRecipient recipient) {
            state.recipients.removeIf(item -> item.getId() != null && item.getId().equals(recipient.getId()));
            SurveyRecipient saved = new SurveyRecipient(
                    recipient.getId() == null ? state.recipients.size() + 1 : recipient.getId(),
                    recipient.getSurveyId(),
                    recipient.getStudentId(),
                    recipient.getAssignedAt(),
                    recipient.getOpenedAt(),
                    recipient.getSubmittedAt()
            );
            state.recipients.add(saved);
            return saved;
        }

        @Override
        public List<SurveyRecipient> saveAll(List<SurveyRecipient> recipients) {
            recipients.forEach(this::save);
            return recipients;
        }
    }

    private static final class CandidatePort implements LoadSurveyRecipientCandidatePort {
        private final InMemorySurveyState state;

        private CandidatePort(InMemorySurveyState state) {
            this.state = state;
        }

        @Override
        public List<Student> loadActiveStudents() {
            return state.candidateStudents.stream()
                    .filter(student -> student.getStatus() == Status.ACTIVE)
                    .filter(student -> student.getUser() != null && Boolean.TRUE.equals(student.getUser().getVerified()))
                    .toList();
        }

        @Override
        public List<Student> loadActiveStudentsByDepartment(Integer departmentId) {
            return state.candidateStudents.stream()
                    .filter(student -> student.getStatus() == Status.ACTIVE)
                    .filter(student -> student.getUser() != null && Boolean.TRUE.equals(student.getUser().getVerified()))
                    .filter(student -> student.getDepartment() != null && student.getDepartment().getId().equals(departmentId))
                    .toList();
        }
    }

    private static final class StudentPort implements LoadStudentPort {
        private final InMemorySurveyState state;

        private StudentPort(InMemorySurveyState state) {
            this.state = state;
        }

        @Override
        public java.util.Optional<Student> loadById(Integer studentId) {
            return state.candidateStudents.stream().filter(student -> student.getId().equals(studentId)).findFirst();
        }
    }

    private static final class AuditPort implements SaveAuditLogPort {
        private final InMemorySurveyState state;

        private AuditPort(InMemorySurveyState state) {
            this.state = state;
        }

        @Override
        public AuditLog save(AuditLog auditLog) {
            state.auditLogs.add(auditLog);
            return auditLog;
        }
    }

    private static final class ManageSurveyQueryPort implements ManageSurveyPort {
        private final InMemorySurveyState state;

        private ManageSurveyQueryPort(InMemorySurveyState state) {
            this.state = state;
        }

        @Override
        public ManagedSurveySearchPage loadPage(ManageSurveysQuery query) {
            return new ManagedSurveySearchPage(
                    List.of(new ManagedSurveySearchItem(
                            state.survey.getId(),
                            state.survey.getTitle(),
                            state.survey.getDescription(),
                            state.survey.getStartDate(),
                            state.survey.getEndDate(),
                            state.survey.getLifecycleState().name(),
                            state.survey.status().name(),
                            state.survey.isHidden(),
                            "ALL_STUDENTS",
                            null,
                            null,
                            state.responseCount,
                            state.recipients.size(),
                            state.recipients.stream().filter(SurveyRecipient::hasOpened).count(),
                            state.recipients.stream().filter(SurveyRecipient::hasSubmitted).count(),
                            0.0
                    )),
                    0,
                    20,
                    1,
                    1,
                    new ManagedSurveyMetrics(1, 0, 0, 0, 0, 0)
            );
        }

        @Override
        public List<Department> loadDepartments() {
            return List.of(
                    new Department(1, "Department 1"),
                    new Department(2, "Department 2")
            );
        }
    }
}
