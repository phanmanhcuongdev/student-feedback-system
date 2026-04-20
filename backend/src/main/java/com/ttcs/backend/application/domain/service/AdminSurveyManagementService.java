package com.ttcs.backend.application.domain.service;

import com.ttcs.backend.application.domain.model.AuditActionType;
import com.ttcs.backend.application.domain.model.AuditLog;
import com.ttcs.backend.application.domain.model.AuditTargetType;
import com.ttcs.backend.application.domain.model.EvaluatorType;
import com.ttcs.backend.application.domain.model.Question;
import com.ttcs.backend.application.domain.model.QuestionType;
import com.ttcs.backend.application.domain.model.Student;
import com.ttcs.backend.application.domain.model.SubjectType;
import com.ttcs.backend.application.domain.model.Survey;
import com.ttcs.backend.application.domain.model.SurveyAssignment;
import com.ttcs.backend.application.domain.model.SurveyLifecycleState;
import com.ttcs.backend.application.domain.model.SurveyRecipient;
import com.ttcs.backend.application.domain.model.SurveyRecipientScope;
import com.ttcs.backend.application.domain.model.SurveyStatus;
import com.ttcs.backend.application.port.in.admin.GetManagedSurveysQuery;
import com.ttcs.backend.application.port.in.admin.ArchiveSurveyUseCase;
import com.ttcs.backend.application.port.in.admin.CloseSurveyUseCase;
import com.ttcs.backend.application.port.in.admin.GetManagedSurveyDetailUseCase;
import com.ttcs.backend.application.port.in.admin.GetManagedSurveysUseCase;
import com.ttcs.backend.application.port.in.admin.GetSurveyManagementDepartmentsUseCase;
import com.ttcs.backend.application.port.in.admin.ManagedSurveyMetricsResult;
import com.ttcs.backend.application.port.in.admin.ManagedSurveyPageResult;
import com.ttcs.backend.application.port.in.admin.PublishSurveyUseCase;
import com.ttcs.backend.application.port.in.admin.SetSurveyHiddenUseCase;
import com.ttcs.backend.application.port.in.admin.SurveyManagementActionResult;
import com.ttcs.backend.application.port.in.admin.SurveyManagementDetailResult;
import com.ttcs.backend.application.port.in.admin.SurveyManagementQuestionResult;
import com.ttcs.backend.application.port.in.admin.SurveyManagementRecipientResult;
import com.ttcs.backend.application.port.in.admin.SurveyManagementSummaryResult;
import com.ttcs.backend.application.port.in.admin.UpdateSurveyCommand;
import com.ttcs.backend.application.port.in.admin.UpdateSurveyQuestionCommand;
import com.ttcs.backend.application.port.in.admin.UpdateSurveyUseCase;
import com.ttcs.backend.application.port.out.LoadQuestionPort;
import com.ttcs.backend.application.port.out.LoadSurveyRecipientCandidatePort;
import com.ttcs.backend.application.port.out.LoadSurveyRecipientPort;
import com.ttcs.backend.application.port.out.LoadSurveyAssignmentPort;
import com.ttcs.backend.application.port.out.LoadSurveyPort;
import com.ttcs.backend.application.port.out.LoadSurveyResponsePort;
import com.ttcs.backend.application.port.out.LoadStudentPort;
import com.ttcs.backend.application.port.out.NotificationCreateCommand;
import com.ttcs.backend.application.port.out.SaveQuestionPort;
import com.ttcs.backend.application.port.out.SaveAuditLogPort;
import com.ttcs.backend.application.port.out.SaveNotificationPort;
import com.ttcs.backend.application.port.out.SaveSurveyRecipientPort;
import com.ttcs.backend.application.port.out.SaveSurveyAssignmentPort;
import com.ttcs.backend.application.port.out.SaveSurveyPort;
import com.ttcs.backend.application.port.out.admin.ManageSurveyPort;
import com.ttcs.backend.application.port.out.admin.ManageSurveysQuery;
import com.ttcs.backend.common.UseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@UseCase
@RequiredArgsConstructor
public class AdminSurveyManagementService implements
        GetManagedSurveysUseCase,
        GetManagedSurveyDetailUseCase,
        GetSurveyManagementDepartmentsUseCase,
        UpdateSurveyUseCase,
        SetSurveyHiddenUseCase,
        CloseSurveyUseCase,
        PublishSurveyUseCase,
        ArchiveSurveyUseCase {

    private static final long CLOSING_SOON_DAYS = 3;

    private final LoadSurveyPort loadSurveyPort;
    private final SaveSurveyPort saveSurveyPort;
    private final LoadQuestionPort loadQuestionPort;
    private final SaveQuestionPort saveQuestionPort;
    private final LoadSurveyAssignmentPort loadSurveyAssignmentPort;
    private final SaveSurveyAssignmentPort saveSurveyAssignmentPort;
    private final LoadSurveyResponsePort loadSurveyResponsePort;
    private final LoadSurveyRecipientPort loadSurveyRecipientPort;
    private final SaveSurveyRecipientPort saveSurveyRecipientPort;
    private final LoadSurveyRecipientCandidatePort loadSurveyRecipientCandidatePort;
    private final LoadStudentPort loadStudentPort;
    private final SaveAuditLogPort saveAuditLogPort;
    private final SaveNotificationPort saveNotificationPort;
    private final ManageSurveyPort manageSurveyPort;

    @Override
    @Transactional(readOnly = true)
    public ManagedSurveyPageResult getSurveys(GetManagedSurveysQuery query) {
        var surveyPage = manageSurveyPort.loadPage(new ManageSurveysQuery(
                query == null ? null : query.keyword(),
                query == null ? null : query.lifecycleState(),
                query == null ? null : query.runtimeStatus(),
                query == null ? null : query.hidden(),
                query == null ? null : query.recipientScope(),
                query == null ? null : query.startDateFrom(),
                query == null ? null : query.endDateTo(),
                query == null ? 0 : query.page(),
                query == null ? 20 : query.size(),
                query == null ? "startDate" : query.sortBy(),
                query == null ? "desc" : query.sortDir()
        ));

        return new ManagedSurveyPageResult(
                surveyPage.items().stream()
                        .map(item -> new SurveyManagementSummaryResult(
                                item.id(),
                                item.title(),
                                item.description(),
                                item.startDate(),
                                item.endDate(),
                                item.lifecycleState(),
                                item.runtimeStatus(),
                                item.hidden(),
                                item.recipientScope(),
                                item.recipientDepartmentId(),
                                item.recipientDepartmentName(),
                                item.responseCount(),
                                item.targetedCount(),
                                item.openedCount(),
                                item.submittedCount(),
                                item.responseRate()
                        ))
                        .toList(),
                surveyPage.page(),
                surveyPage.size(),
                surveyPage.totalElements(),
                surveyPage.totalPages(),
                new ManagedSurveyMetricsResult(
                        surveyPage.metrics().totalSurveys(),
                        surveyPage.metrics().totalDrafts(),
                        surveyPage.metrics().totalPublished(),
                        surveyPage.metrics().totalOpen(),
                        surveyPage.metrics().totalClosed(),
                        surveyPage.metrics().totalHidden()
                )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<com.ttcs.backend.application.domain.model.Department> getDepartments() {
        return manageSurveyPort.loadDepartments();
    }

    @Override
    @Transactional(readOnly = true)
    public SurveyManagementDetailResult getSurvey(Integer surveyId) {
        Survey survey = loadSurveyPort.loadById(surveyId).orElseThrow(() -> new IllegalArgumentException("SURVEY_NOT_FOUND"));
        RecipientInfo recipient = recipientInfo(surveyId);
        long responseCount = loadSurveyResponsePort.countBySurveyId(surveyId);
        List<SurveyRecipient> recipients = loadSurveyRecipientPort.loadBySurveyId(surveyId);
        ParticipationSummary participation = summarizeParticipation(recipients);
        return new SurveyManagementDetailResult(
                survey.getId(),
                survey.getTitle(),
                survey.getDescription(),
                survey.getStartDate(),
                survey.getEndDate(),
                survey.getLifecycleState().name(),
                survey.status().name(),
                survey.isHidden(),
                recipient.scope().name(),
                recipient.departmentId(),
                recipient.departmentName(),
                responseCount,
                participation.targetedCount(),
                participation.openedCount(),
                participation.submittedCount(),
                participation.responseRate(),
                loadQuestionPort.loadBySurveyId(surveyId).stream()
                        .map(question -> new SurveyManagementQuestionResult(question.getId(), question.getContent(), question.getType().name()))
                        .toList(),
                recipients.stream()
                        .filter(item -> !item.hasSubmitted())
                        .map(this::toPendingRecipient)
                        .toList()
        );
    }

    @Override
    @Transactional
    public SurveyManagementActionResult updateSurvey(UpdateSurveyCommand command) {
        if (command == null || command.surveyId() == null || isBlank(command.title())) {
            return SurveyManagementActionResult.fail("INVALID_INPUT", "Survey title is required.");
        }
        if (command.recipientScope() == null) {
            return SurveyManagementActionResult.fail("INVALID_INPUT", "Recipient scope is required.");
        }

        Survey existing = loadSurveyPort.loadById(command.surveyId()).orElse(null);
        if (existing == null) {
            return SurveyManagementActionResult.fail("SURVEY_NOT_FOUND", "Survey was not found.");
        }
        if (!existing.isDraft()) {
            return SurveyManagementActionResult.fail("SURVEY_NOT_EDITABLE", "Only draft surveys can be edited.");
        }
        if (command.startDate() != null && command.endDate() != null && command.endDate().isBefore(command.startDate())) {
            return SurveyManagementActionResult.fail("INVALID_INPUT", "End date must be after start date.");
        }
        if (command.questions() == null || command.questions().isEmpty()) {
            return SurveyManagementActionResult.fail("INVALID_INPUT", "At least one question is required.");
        }
        if (command.questions().stream().anyMatch(question -> question == null || isBlank(question.content()))) {
            return SurveyManagementActionResult.fail("INVALID_INPUT", "All questions must have content.");
        }
        if (command.recipientScope() == SurveyRecipientScope.DEPARTMENT && command.recipientDepartmentId() == null) {
            return SurveyManagementActionResult.fail("INVALID_INPUT", "Recipient department is required for department scope.");
        }

        long responseCount = loadSurveyResponsePort.countBySurveyId(command.surveyId());
        if (responseCount > 0) {
            return SurveyManagementActionResult.fail("SURVEY_LOCKED", "Draft survey already has responses and cannot be edited.");
        }

        Survey updatedSurvey = new Survey(
                existing.getId(),
                command.title().trim(),
                command.description(),
                command.startDate(),
                command.endDate(),
                existing.getCreatedBy(),
                existing.isHidden(),
                existing.getLifecycleState()
        );
        saveSurveyPort.save(updatedSurvey);

        saveQuestionPort.replaceSurveyQuestions(
                command.surveyId(),
                command.questions().stream()
                        .map(question -> new Question(
                                null,
                                command.surveyId(),
                                question.content(),
                                QuestionType.valueOf(question.type()),
                                question.questionBankEntryId()
                        ))
                        .toList()
        );

        saveSurveyAssignmentPort.replaceAssignments(
                command.surveyId(),
                List.of(toAssignment(updatedSurvey, command.recipientScope(), command.recipientDepartmentId()))
        );

        return SurveyManagementActionResult.ok("SURVEY_UPDATED", "Survey draft updated successfully.");
    }

    @Override
    @Transactional
    public SurveyManagementActionResult publishSurvey(Integer surveyId, Integer actorUserId) {
        if (surveyId == null || actorUserId == null) {
            return SurveyManagementActionResult.fail("INVALID_INPUT", "Survey id and actor id are required.");
        }
        Survey survey = loadSurveyPort.loadById(surveyId).orElse(null);
        if (survey == null) {
            return SurveyManagementActionResult.fail("SURVEY_NOT_FOUND", "Survey was not found.");
        }
        if (!survey.isDraft()) {
            return SurveyManagementActionResult.fail("INVALID_TRANSITION", "Only draft surveys can be published.");
        }
        if (!hasPublishableSchedule(survey)) {
            return SurveyManagementActionResult.fail("SURVEY_NOT_READY", "Published surveys require a valid start and end date.");
        }

        List<Question> questions = loadQuestionPort.loadBySurveyId(surveyId);
        if (!hasPublishableQuestions(questions)) {
            return SurveyManagementActionResult.fail("SURVEY_NOT_READY", "Published surveys require at least one question with content.");
        }

        List<SurveyAssignment> assignments = loadSurveyAssignmentPort.loadBySurveyId(surveyId);
        if (!hasPublishableAssignments(assignments)) {
            return SurveyManagementActionResult.fail("SURVEY_NOT_READY", "Published surveys require at least one valid recipient assignment.");
        }

        Survey publishedSurvey = new Survey(
                survey.getId(),
                survey.getTitle(),
                survey.getDescription(),
                survey.getStartDate(),
                survey.getEndDate(),
                survey.getCreatedBy(),
                survey.isHidden(),
                SurveyLifecycleState.PUBLISHED
        );
        saveSurveyPort.save(publishedSurvey);
        createRecipientsForPublishedSurvey(surveyId, assignments);
        List<SurveyRecipient> recipients = loadSurveyRecipientPort.loadBySurveyId(surveyId);
        saveAuditLogPort.save(new AuditLog(
                null,
                actorUserId,
                AuditActionType.SURVEY_PUBLISHED,
                AuditTargetType.SURVEY,
                survey.getId(),
                "Published survey",
                "title=" + survey.getTitle() + "; recipients=" + recipients.size(),
                SurveyLifecycleState.DRAFT.name(),
                SurveyLifecycleState.PUBLISHED.name(),
                null
        ));
        List<Integer> recipientUserIds = recipientUserIds(recipients);
        if (!recipientUserIds.isEmpty()) {
            saveNotificationPort.create(new NotificationCreateCommand(
                    "SURVEY_PUBLISHED",
                    "New survey available",
                    "A new survey is available for responses: " + survey.getTitle(),
                    survey.getId(),
                    "Open survey",
                    actorUserId,
                    "recipients=" + recipients.size(),
                    recipientUserIds
            ));
            createDeadlineReminderForClosingSoonSurvey(publishedSurvey, recipientUserIds);
        }

        return SurveyManagementActionResult.ok("SURVEY_PUBLISHED", "Survey published successfully.");
    }

    @Override
    @Transactional
    public SurveyManagementActionResult setHidden(Integer surveyId, boolean hidden, Integer actorUserId) {
        if (surveyId == null || actorUserId == null) {
            return SurveyManagementActionResult.fail("INVALID_INPUT", "Survey id and actor id are required.");
        }
        Survey survey = loadSurveyPort.loadById(surveyId).orElse(null);
        if (survey == null) {
            return SurveyManagementActionResult.fail("SURVEY_NOT_FOUND", "Survey was not found.");
        }
        if (!(survey.isPublished() || survey.isLifecycleClosed())) {
            return SurveyManagementActionResult.fail("INVALID_TRANSITION", "Visibility can only be changed for published or closed surveys.");
        }

        saveSurveyPort.save(new Survey(
                survey.getId(),
                survey.getTitle(),
                survey.getDescription(),
                survey.getStartDate(),
                survey.getEndDate(),
                survey.getCreatedBy(),
                hidden,
                survey.getLifecycleState()
        ));
        saveAuditLogPort.save(new AuditLog(
                null,
                actorUserId,
                AuditActionType.SURVEY_VISIBILITY_CHANGED,
                AuditTargetType.SURVEY,
                survey.getId(),
                hidden ? "Hid survey" : "Made survey visible",
                "title=" + survey.getTitle() + "; lifecycleState=" + survey.getLifecycleState().name(),
                visibilityState(survey.isHidden()),
                visibilityState(hidden),
                null
        ));
        return SurveyManagementActionResult.ok(hidden ? "SURVEY_HIDDEN" : "SURVEY_VISIBLE", hidden ? "Survey hidden successfully." : "Survey is visible again.");
    }

    @Override
    @Transactional
    public SurveyManagementActionResult closeSurvey(Integer surveyId, Integer actorUserId) {
        if (surveyId == null || actorUserId == null) {
            return SurveyManagementActionResult.fail("INVALID_INPUT", "Survey id and actor id are required.");
        }
        Survey survey = loadSurveyPort.loadById(surveyId).orElse(null);
        if (survey == null) {
            return SurveyManagementActionResult.fail("SURVEY_NOT_FOUND", "Survey was not found.");
        }
        if (!survey.isPublished()) {
            return SurveyManagementActionResult.fail("INVALID_TRANSITION", "Only published surveys can be closed.");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextEndDate = survey.getEndDate() == null || survey.getEndDate().isAfter(now)
                ? now
                : survey.getEndDate();
        saveSurveyPort.save(new Survey(
                survey.getId(),
                survey.getTitle(),
                survey.getDescription(),
                survey.getStartDate(),
                nextEndDate,
                survey.getCreatedBy(),
                survey.isHidden(),
                SurveyLifecycleState.CLOSED
        ));
        saveAuditLogPort.save(new AuditLog(
                null,
                actorUserId,
                AuditActionType.SURVEY_CLOSED,
                AuditTargetType.SURVEY,
                survey.getId(),
                "Closed survey",
                "title=" + survey.getTitle(),
                survey.getLifecycleState().name(),
                SurveyLifecycleState.CLOSED.name(),
                null
        ));
        return SurveyManagementActionResult.ok("SURVEY_CLOSED", "Survey closed successfully.");
    }

    @Override
    @Transactional
    public SurveyManagementActionResult archiveSurvey(Integer surveyId, Integer actorUserId) {
        if (surveyId == null || actorUserId == null) {
            return SurveyManagementActionResult.fail("INVALID_INPUT", "Survey id and actor id are required.");
        }
        Survey survey = loadSurveyPort.loadById(surveyId).orElse(null);
        if (survey == null) {
            return SurveyManagementActionResult.fail("SURVEY_NOT_FOUND", "Survey was not found.");
        }
        if (!survey.isLifecycleClosed()) {
            return SurveyManagementActionResult.fail("INVALID_TRANSITION", "Only closed surveys can be archived.");
        }

        saveSurveyPort.save(new Survey(
                survey.getId(),
                survey.getTitle(),
                survey.getDescription(),
                survey.getStartDate(),
                survey.getEndDate(),
                survey.getCreatedBy(),
                survey.isHidden(),
                SurveyLifecycleState.ARCHIVED
        ));
        saveAuditLogPort.save(new AuditLog(
                null,
                actorUserId,
                AuditActionType.SURVEY_ARCHIVED,
                AuditTargetType.SURVEY,
                survey.getId(),
                "Archived survey",
                "title=" + survey.getTitle(),
                survey.getLifecycleState().name(),
                SurveyLifecycleState.ARCHIVED.name(),
                null
        ));
        return SurveyManagementActionResult.ok("SURVEY_ARCHIVED", "Survey archived successfully.");
    }

    private SurveyManagementSummaryResult toSummary(Survey survey) {
        RecipientInfo recipient = recipientInfo(survey.getId());
        List<SurveyRecipient> recipients = loadSurveyRecipientPort.loadBySurveyId(survey.getId());
        ParticipationSummary participation = summarizeParticipation(recipients);
        return new SurveyManagementSummaryResult(
                survey.getId(),
                survey.getTitle(),
                survey.getDescription(),
                survey.getStartDate(),
                survey.getEndDate(),
                survey.getLifecycleState().name(),
                survey.status().name(),
                survey.isHidden(),
                recipient.scope().name(),
                recipient.departmentId(),
                recipient.departmentName(),
                loadSurveyResponsePort.countBySurveyId(survey.getId()),
                participation.targetedCount(),
                participation.openedCount(),
                participation.submittedCount(),
                participation.responseRate()
        );
    }

    private RecipientInfo recipientInfo(Integer surveyId) {
        List<SurveyAssignment> assignments = loadSurveyAssignmentPort.loadBySurveyId(surveyId);
        if (assignments.isEmpty()) {
            return new RecipientInfo(SurveyRecipientScope.ALL_STUDENTS, null, null);
        }

        SurveyAssignment assignment = assignments.getFirst();
        if (assignment.getEvaluatorType() == EvaluatorType.STUDENT && assignment.getSubjectType() == SubjectType.DEPARTMENT) {
            Integer departmentId = assignment.getSubjectValue();
            String departmentName = departmentId == null
                    ? null
                    : manageSurveyPort.loadDepartments().stream()
                    .filter(department -> department.getId().equals(departmentId))
                    .map(department -> department.getName())
                    .findFirst()
                    .orElse(null);
            return new RecipientInfo(SurveyRecipientScope.DEPARTMENT, departmentId, departmentName);
        }
        return new RecipientInfo(SurveyRecipientScope.ALL_STUDENTS, null, null);
    }

    private SurveyAssignment toAssignment(Survey survey, SurveyRecipientScope scope, Integer departmentId) {
        if (scope == SurveyRecipientScope.DEPARTMENT) {
            return new SurveyAssignment(null, survey, EvaluatorType.STUDENT, null, SubjectType.DEPARTMENT, departmentId);
        }
        return new SurveyAssignment(null, survey, EvaluatorType.STUDENT, null, SubjectType.ALL, null);
    }

    private boolean hasPublishableSchedule(Survey survey) {
        return survey.getStartDate() != null
                && survey.getEndDate() != null
                && !survey.getEndDate().isBefore(survey.getStartDate());
    }

    private boolean hasPublishableQuestions(List<Question> questions) {
        return questions != null
                && !questions.isEmpty()
                && questions.stream().allMatch(question -> question != null && !isBlank(question.getContent()));
    }

    private boolean hasPublishableAssignments(List<SurveyAssignment> assignments) {
        return assignments != null
                && assignments.stream().anyMatch(assignment ->
                assignment != null
                        && assignment.getEvaluatorType() == EvaluatorType.STUDENT
                        && (assignment.getSubjectType() == SubjectType.ALL
                        || (assignment.getSubjectType() == SubjectType.DEPARTMENT && assignment.getSubjectValue() != null))
        );
    }

    private void createRecipientsForPublishedSurvey(Integer surveyId, List<SurveyAssignment> assignments) {
        LocalDateTime assignedAt = LocalDateTime.now();
        java.util.Set<Integer> existingStudentIds = loadSurveyRecipientPort.loadBySurveyId(surveyId).stream()
                .map(SurveyRecipient::getStudentId)
                .collect(java.util.stream.Collectors.toSet());
        List<Student> students = resolveRecipientCandidates(assignments);
        saveSurveyRecipientPort.saveAll(students.stream()
                .filter(student -> !existingStudentIds.contains(student.getId()))
                .map(student -> new SurveyRecipient(
                        null,
                        surveyId,
                        student.getId(),
                        assignedAt,
                        null,
                        null
                ))
                .toList());
    }

    private List<Student> resolveRecipientCandidates(List<SurveyAssignment> assignments) {
        if (assignments == null || assignments.isEmpty()) {
            return List.of();
        }

        SurveyAssignment assignment = assignments.getFirst();
        if (assignment.getSubjectType() == SubjectType.DEPARTMENT && assignment.getSubjectValue() != null) {
            return loadSurveyRecipientCandidatePort.loadActiveStudentsByDepartment(assignment.getSubjectValue());
        }
        return loadSurveyRecipientCandidatePort.loadActiveStudents();
    }

    private void createDeadlineReminderForClosingSoonSurvey(Survey survey, List<Integer> recipientUserIds) {
        LocalDateTime now = LocalDateTime.now();
        if (survey.statusAt(now) != SurveyStatus.OPEN || !isWithinNextDays(survey.getEndDate(), now, CLOSING_SOON_DAYS)) {
            return;
        }

        saveNotificationPort.create(new NotificationCreateCommand(
                "SURVEY_DEADLINE_REMINDER",
                "Survey closing soon",
                "The survey \"" + survey.getTitle() + "\" is closing soon. Submit before the deadline.",
                survey.getId(),
                "Respond now",
                null,
                "deadline=" + safeEventAt(survey.getEndDate(), now),
                recipientUserIds
        ));
    }

    private List<Integer> recipientUserIds(List<SurveyRecipient> recipients) {
        List<Integer> studentIds = recipients.stream()
                .map(SurveyRecipient::getStudentId)
                .distinct()
                .toList();
        return loadStudentPort.loadByIds(studentIds).stream()
                .map(Student::getUser)
                .filter(java.util.Objects::nonNull)
                .map(com.ttcs.backend.application.domain.model.User::getId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
    }

    private boolean isWithinNextDays(LocalDateTime value, LocalDateTime now, long days) {
        if (value == null || value.isBefore(now)) {
            return false;
        }

        return Duration.between(now, value).toDays() <= days;
    }

    private LocalDateTime safeEventAt(LocalDateTime preferred, LocalDateTime fallback) {
        return preferred != null ? preferred : fallback;
    }

    private ParticipationSummary summarizeParticipation(List<SurveyRecipient> recipients) {
        long targetedCount = recipients.size();
        long openedCount = recipients.stream().filter(SurveyRecipient::hasOpened).count();
        long submittedCount = recipients.stream().filter(SurveyRecipient::hasSubmitted).count();
        double responseRate = targetedCount == 0 ? 0.0 : (submittedCount * 100.0) / targetedCount;
        return new ParticipationSummary(targetedCount, openedCount, submittedCount, responseRate);
    }

    private SurveyManagementRecipientResult toPendingRecipient(SurveyRecipient recipient) {
        Student student = loadStudentPort.loadById(recipient.getStudentId())
                .orElseThrow(() -> new IllegalArgumentException("STUDENT_NOT_FOUND"));
        return new SurveyManagementRecipientResult(
                student.getId(),
                student.getName(),
                student.getStudentCode(),
                student.getDepartment() == null ? null : student.getDepartment().getName(),
                recipient.status().name(),
                recipient.getOpenedAt(),
                recipient.getSubmittedAt()
        );
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String visibilityState(boolean hidden) {
        return hidden ? "HIDDEN" : "VISIBLE";
    }

    private record RecipientInfo(SurveyRecipientScope scope, Integer departmentId, String departmentName) {
    }

    private record ParticipationSummary(long targetedCount, long openedCount, long submittedCount, double responseRate) {
    }
}
