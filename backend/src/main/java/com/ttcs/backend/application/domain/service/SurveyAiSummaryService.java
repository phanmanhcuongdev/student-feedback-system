package com.ttcs.backend.application.domain.service;

import com.ttcs.backend.application.domain.exception.SurveyNotFoundException;
import com.ttcs.backend.application.domain.model.EvaluatorType;
import com.ttcs.backend.application.domain.model.Role;
import com.ttcs.backend.application.domain.model.SubjectType;
import com.ttcs.backend.application.domain.model.Survey;
import com.ttcs.backend.application.domain.model.SurveyAiSummaryJobStatus;
import com.ttcs.backend.application.domain.model.SurveyAssignment;
import com.ttcs.backend.application.domain.model.Teacher;
import com.ttcs.backend.application.port.in.resultview.GenerateSurveyAiSummaryUseCase;
import com.ttcs.backend.application.port.in.resultview.GetSurveyAiSummaryUseCase;
import com.ttcs.backend.application.port.in.resultview.SurveyAiSummaryViewResult;
import com.ttcs.backend.application.port.out.GenerateSurveyCommentSummaryPort;
import com.ttcs.backend.application.port.out.LoadSurveyAiSummaryPort;
import com.ttcs.backend.application.port.out.LoadSurveyAssignmentPort;
import com.ttcs.backend.application.port.out.LoadSurveyPort;
import com.ttcs.backend.application.port.out.LoadTeacherByUserIdPort;
import com.ttcs.backend.application.port.out.SaveSurveyAiSummaryPort;
import com.ttcs.backend.application.port.out.SurveyCommentSummaryCommand;
import com.ttcs.backend.application.port.out.SurveyCommentSummaryResult;
import com.ttcs.backend.common.UseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static org.springframework.http.HttpStatus.FORBIDDEN;

@UseCase
@RequiredArgsConstructor
public class SurveyAiSummaryService implements GenerateSurveyAiSummaryUseCase, GetSurveyAiSummaryUseCase {

    private final LoadSurveyPort loadSurveyPort;
    private final LoadSurveyAssignmentPort loadSurveyAssignmentPort;
    private final LoadTeacherByUserIdPort loadTeacherByUserIdPort;
    private final LoadSurveyAiSummaryPort loadSurveyAiSummaryPort;
    private final SaveSurveyAiSummaryPort saveSurveyAiSummaryPort;
    private final GenerateSurveyCommentSummaryPort generateSurveyCommentSummaryPort;
    private final ExecutorService surveyAiSummaryExecutor;

    @Override
    public SurveyAiSummaryViewResult getSummary(Integer surveyId, Integer viewerUserId, Role viewerRole) {
        authorizeAccess(surveyId, viewerUserId, viewerRole);

        var latestJob = loadSurveyAiSummaryPort.loadLatestJob(surveyId).orElse(null);
        var latestSummary = resolveSummary(surveyId, latestJob);
        if (latestJob != null) {
            return toView(surveyId, latestJob, latestSummary);
        }
        if (latestSummary != null) {
            return new SurveyAiSummaryViewResult(
                    surveyId,
                    SurveyAiSummaryJobStatus.COMPLETED.name(),
                    null,
                    latestSummary.commentCount(),
                    latestSummary.summaryText(),
                    latestSummary.highlights(),
                    latestSummary.concerns(),
                    latestSummary.actions(),
                    latestSummary.modelName(),
                    null,
                    latestSummary.createdAt(),
                    latestSummary.createdAt(),
                    latestSummary.createdAt()
            );
        }
        return new SurveyAiSummaryViewResult(surveyId, "NOT_REQUESTED", null, 0, null, List.of(), List.of(), List.of(), null, null, null, null, null);
    }

    @Override
    public synchronized SurveyAiSummaryViewResult generate(Integer surveyId, Integer viewerUserId, Role viewerRole) {
        Survey survey = authorizeAccess(surveyId, viewerUserId, viewerRole);
        var payload = loadSurveyAiSummaryPort.loadSurveySummaryPayload(surveyId);
        String sourceHash = computeSourceHash(payload);

        var latestSummary = loadSurveyAiSummaryPort.loadLatestSummary(surveyId).orElse(null);
        if (latestSummary != null && sourceHash.equals(latestSummary.sourceHash())) {
            return new SurveyAiSummaryViewResult(
                    surveyId,
                    SurveyAiSummaryJobStatus.COMPLETED.name(),
                    null,
                    latestSummary.commentCount(),
                    latestSummary.summaryText(),
                    latestSummary.highlights(),
                    latestSummary.concerns(),
                    latestSummary.actions(),
                    latestSummary.modelName(),
                    null,
                    latestSummary.createdAt(),
                    latestSummary.createdAt(),
                    latestSummary.createdAt()
            );
        }

        var latestJob = loadSurveyAiSummaryPort.loadLatestJob(surveyId).orElse(null);
        if (latestJob != null
                && sourceHash.equals(latestJob.sourceHash())
                && (latestJob.status() == SurveyAiSummaryJobStatus.QUEUED || latestJob.status() == SurveyAiSummaryJobStatus.PROCESSING)) {
            return toView(surveyId, latestJob, null);
        }

        if (payload.commentCount() == 0) {
            var summary = saveSurveyAiSummaryPort.saveSummary(
                    surveyId,
                    sourceHash,
                    "local-empty-feedback",
                    0,
                    "Khong co y kien dang text de tom tat cho khao sat nay.",
                    List.of(),
                    List.of(),
                    List.of("Thu thap them cau tra loi text neu admin can tong hop y kien chi tiet hon."),
                    viewerUserId
            );
            return new SurveyAiSummaryViewResult(
                    surveyId,
                    SurveyAiSummaryJobStatus.COMPLETED.name(),
                    null,
                    summary.commentCount(),
                    summary.summaryText(),
                    summary.highlights(),
                    summary.concerns(),
                    summary.actions(),
                    summary.modelName(),
                    null,
                    summary.createdAt(),
                    summary.createdAt(),
                    summary.createdAt()
            );
        }

        var job = saveSurveyAiSummaryPort.createJob(surveyId, sourceHash, payload.commentCount(), viewerUserId);
        surveyAiSummaryExecutor.submit(() -> processJob(job.id(), survey, payload, sourceHash, viewerUserId));
        return toView(surveyId, job, null);
    }

    private void processJob(Integer jobId, Survey survey, LoadSurveyAiSummaryPort.SurveyAiSummaryPayload payload, String sourceHash, Integer viewerUserId) {
        try {
            saveSurveyAiSummaryPort.markJobProcessing(jobId, LocalDateTime.now());
            SurveyCommentSummaryResult result = generateSurveyCommentSummaryPort.generateSummary(
                    new SurveyCommentSummaryCommand(
                            survey.getId(),
                            survey.getTitle(),
                            payload.commentCount(),
                            payload.comments().stream()
                                    .map(item -> "Cau hoi: " + item.questionContent() + "\nY kien: " + item.comment())
                                    .toList()
                    )
            );
            var summary = saveSurveyAiSummaryPort.saveSummary(
                    survey.getId(),
                    sourceHash,
                    result.modelName(),
                    payload.commentCount(),
                    result.summary(),
                    result.highlights(),
                    result.concerns(),
                    result.actions(),
                    viewerUserId
            );
            saveSurveyAiSummaryPort.markJobCompleted(jobId, summary.id(), LocalDateTime.now());
        } catch (Exception exception) {
            saveSurveyAiSummaryPort.markJobFailed(jobId, exception.getMessage(), LocalDateTime.now());
        }
    }

    private Survey authorizeAccess(Integer surveyId, Integer viewerUserId, Role viewerRole) {
        Survey survey = loadSurveyPort.loadById(surveyId)
                .orElseThrow(() -> new SurveyNotFoundException(surveyId));

        if (viewerRole == Role.ADMIN) {
            return survey;
        }

        Teacher teacher = requireTeacher(viewerUserId, viewerRole);
        Integer teacherDepartmentId = teacher.getDepartment() != null ? teacher.getDepartment().getId() : null;
        if (teacherDepartmentId == null) {
            throw new ResponseStatusException(FORBIDDEN, "Teacher department scope is unavailable");
        }
        if (!isTeacherInScope(surveyId, teacherDepartmentId)) {
            throw new ResponseStatusException(FORBIDDEN, "You are not allowed to view AI summary for this survey");
        }
        return survey;
    }

    private Teacher requireTeacher(Integer viewerUserId, Role viewerRole) {
        if (viewerUserId == null || viewerRole != Role.TEACHER) {
            throw new ResponseStatusException(FORBIDDEN, "You are not allowed to view survey AI summaries");
        }

        return loadTeacherByUserIdPort.loadByUserId(viewerUserId)
                .orElseThrow(() -> new ResponseStatusException(FORBIDDEN, "Teacher profile not found"));
    }

    private boolean isTeacherInScope(Integer surveyId, Integer teacherDepartmentId) {
        List<SurveyAssignment> assignments = loadSurveyAssignmentPort.loadBySurveyId(surveyId);
        return assignments.stream().anyMatch(assignment ->
                assignment != null
                        && assignment.getEvaluatorType() == EvaluatorType.STUDENT
                        && assignment.getSubjectType() == SubjectType.DEPARTMENT
                        && teacherDepartmentId.equals(assignment.getSubjectValue())
        );
    }

    private LoadSurveyAiSummaryPort.SurveyAiSummaryRecord resolveSummary(Integer surveyId, LoadSurveyAiSummaryPort.SurveyAiSummaryJobRecord latestJob) {
        if (latestJob != null && latestJob.summaryId() != null) {
            return loadSurveyAiSummaryPort.loadSummaryById(latestJob.summaryId()).orElse(null);
        }
        return loadSurveyAiSummaryPort.loadLatestSummary(surveyId).orElse(null);
    }

    private SurveyAiSummaryViewResult toView(Integer surveyId,
                                             LoadSurveyAiSummaryPort.SurveyAiSummaryJobRecord job,
                                             LoadSurveyAiSummaryPort.SurveyAiSummaryRecord summary) {
        return new SurveyAiSummaryViewResult(
                surveyId,
                job.status().name(),
                job.id(),
                summary != null ? summary.commentCount() : job.commentCount(),
                summary != null ? summary.summaryText() : null,
                summary != null ? summary.highlights() : List.of(),
                summary != null ? summary.concerns() : List.of(),
                summary != null ? summary.actions() : List.of(),
                summary != null ? summary.modelName() : null,
                job.errorMessage(),
                job.createdAt(),
                job.startedAt(),
                job.finishedAt()
        );
    }

    private String computeSourceHash(LoadSurveyAiSummaryPort.SurveyAiSummaryPayload payload) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update((payload.surveyId() + "|" + payload.commentCount() + "|" + payload.surveyTitle()).getBytes(StandardCharsets.UTF_8));
            for (LoadSurveyAiSummaryPort.QuestionCommentPayload comment : payload.comments()) {
                digest.update((comment.questionId() + "|" + comment.questionContent() + "|" + comment.comment()).getBytes(StandardCharsets.UTF_8));
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to compute AI summary source hash", exception);
        }
    }
}
