package com.ttcs.backend.application.domain.service;

import com.ttcs.backend.application.domain.exception.SurveyNotFoundException;
import com.ttcs.backend.application.domain.model.EvaluatorType;
import com.ttcs.backend.application.domain.model.Role;
import com.ttcs.backend.application.domain.model.SubjectType;
import com.ttcs.backend.application.domain.model.Survey;
import com.ttcs.backend.application.domain.model.SurveyAiSummaryJobStatus;
import com.ttcs.backend.application.domain.model.SurveyAssignment;
import com.ttcs.backend.application.domain.model.Lecturer;
import com.ttcs.backend.application.port.in.resultview.GenerateSurveyAiSummaryUseCase;
import com.ttcs.backend.application.port.in.resultview.GetSurveyAiSummaryUseCase;
import com.ttcs.backend.application.port.in.resultview.SurveyAiSummaryViewResult;
import com.ttcs.backend.application.port.out.GenerateSurveyCommentSummaryPort;
import com.ttcs.backend.application.port.out.LoadSurveyAiSummaryPort;
import com.ttcs.backend.application.port.out.LoadSurveyAssignmentPort;
import com.ttcs.backend.application.port.out.LoadLecturerByUserIdPort;
import com.ttcs.backend.application.port.out.LoadSurveyPort;
import com.ttcs.backend.application.port.out.SaveSurveyAiSummaryPort;
import com.ttcs.backend.application.port.out.SurveyCommentSummaryCommand;
import com.ttcs.backend.application.port.out.SurveyCommentSummaryResult;
import com.ttcs.backend.common.UseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.springframework.http.HttpStatus.FORBIDDEN;

@UseCase
@RequiredArgsConstructor
@Slf4j
public class SurveyAiSummaryService implements GenerateSurveyAiSummaryUseCase, GetSurveyAiSummaryUseCase {

    private final LoadSurveyPort loadSurveyPort;
    private final LoadSurveyAssignmentPort loadSurveyAssignmentPort;
    private final LoadLecturerByUserIdPort loadLecturerByUserIdPort;
    private final LoadSurveyAiSummaryPort loadSurveyAiSummaryPort;
    private final GenerateSurveyCommentSummaryPort generateSurveyCommentSummaryPort;
    private final SaveSurveyAiSummaryPort saveSurveyAiSummaryPort;
    private final ObjectProvider<SurveyAiSummaryService> selfProvider;

    @Override
    public SurveyAiSummaryViewResult getSummary(Integer surveyId, Integer viewerUserId, Role viewerRole) {
        authorizeAccess(surveyId, viewerUserId, viewerRole);

        var latestJob = loadSurveyAiSummaryPort.loadLatestJob(surveyId).orElse(null);
        var latestSummary = resolveSummary(surveyId, latestJob);
        if (latestJob != null) {
            return toView(surveyId, latestJob, latestSummary);
        }
        if (latestSummary != null) {
            return completedView(surveyId, latestSummary);
        }
        return new SurveyAiSummaryViewResult(surveyId, "NOT_REQUESTED", null, 0, null, List.of(), List.of(), List.of(), null, null, null, null);
    }

    @Override
    public SurveyAiSummaryViewResult generate(Integer surveyId, Integer viewerUserId, Role viewerRole) {
        Survey survey = authorizeAccess(surveyId, viewerUserId, viewerRole);
        var payload = loadSurveyAiSummaryPort.loadSurveySummaryPayload(surveyId);
        String sourceHash = computeSourceHash(payload);

        var latestSummary = loadSurveyAiSummaryPort.loadLatestSummary(surveyId).orElse(null);
        if (latestSummary != null && sourceHash.equals(latestSummary.sourceHash())) {
            return completedView(surveyId, latestSummary);
        }

        var latestJob = loadSurveyAiSummaryPort.loadLatestJob(surveyId).orElse(null);
        if (latestJob != null
                && sourceHash.equals(latestJob.sourceHash())
                && (latestJob.status() == SurveyAiSummaryJobStatus.QUEUED || latestJob.status() == SurveyAiSummaryJobStatus.PROCESSING)) {
            return toView(surveyId, latestJob, null);
        }

        var activeJob = loadSurveyAiSummaryPort.loadActiveJob(surveyId).orElse(null);
        if (activeJob != null) {
            log.warn(
                    "Skip AI summary generation for surveyId={} because jobId={} is already {}",
                    surveyId,
                    activeJob.id(),
                    activeJob.status()
            );
            return toView(surveyId, activeJob, null);
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
            return completedView(surveyId, summary);
        }

        var job = saveSurveyAiSummaryPort.createJob(surveyId, sourceHash, payload.commentCount(), viewerUserId);
        try {
            selfProvider.getObject().processJob(job.id(), survey, payload, sourceHash, viewerUserId);
        } catch (Exception exception) {
            saveSurveyAiSummaryPort.markJobFailed(job.id(), errorMessage(exception), LocalDateTime.now());
        }
        return toView(surveyId, job, null);
    }

    @Async("aiTaskExecutor")
    public CompletableFuture<Void> processJob(Integer jobId,
                                              Survey survey,
                                              LoadSurveyAiSummaryPort.SurveyAiSummaryPayload payload,
                                              String sourceHash,
                                              Integer viewerUserId) {
        try {
            boolean claimed = saveSurveyAiSummaryPort.markJobProcessingIfNoActiveJob(jobId, survey.getId(), LocalDateTime.now());
            if (!claimed) {
                String message = "Another AI summary job is already queued or processing for surveyId=" + survey.getId();
                log.warn("Skip AI summary jobId={} for surveyId={}: {}", jobId, survey.getId(), message);
                saveSurveyAiSummaryPort.markJobFailed(jobId, message, LocalDateTime.now());
                return CompletableFuture.completedFuture(null);
            }

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
            saveSurveyAiSummaryPort.markJobFailed(jobId, errorMessage(exception), LocalDateTime.now());
        }
        return CompletableFuture.completedFuture(null);
    }

    private Survey authorizeAccess(Integer surveyId, Integer viewerUserId, Role viewerRole) {
        Survey survey = loadSurveyPort.loadById(surveyId)
                .orElseThrow(() -> new SurveyNotFoundException(surveyId));

        if (viewerRole == Role.ADMIN) {
            return survey;
        }

        Lecturer lecturer = requireLecturer(viewerUserId, viewerRole);
        Integer lecturerDepartmentId = lecturer.getDepartment() != null ? lecturer.getDepartment().getId() : null;
        if (lecturerDepartmentId == null) {
            throw new ResponseStatusException(FORBIDDEN, "Lecturer department scope is unavailable");
        }
        if (!isLecturerInScope(surveyId, lecturerDepartmentId)) {
            throw new ResponseStatusException(FORBIDDEN, "You are not allowed to view AI summary for this survey");
        }
        return survey;
    }

    private Lecturer requireLecturer(Integer viewerUserId, Role viewerRole) {
        if (viewerUserId == null || viewerRole != Role.LECTURER) {
            throw new ResponseStatusException(FORBIDDEN, "You are not allowed to view survey AI summaries");
        }

        return loadLecturerByUserIdPort.loadByUserId(viewerUserId)
                .orElseThrow(() -> new ResponseStatusException(FORBIDDEN, "Lecturer profile not found"));
    }

    private boolean isLecturerInScope(Integer surveyId, Integer lecturerDepartmentId) {
        List<SurveyAssignment> assignments = loadSurveyAssignmentPort.loadBySurveyId(surveyId);
        return assignments.stream().anyMatch(assignment ->
                assignment != null
                        && assignment.getEvaluatorType() == EvaluatorType.STUDENT
                        && assignment.getSubjectType() == SubjectType.DEPARTMENT
                        && lecturerDepartmentId.equals(assignment.getSubjectValue())
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
                job.errorMessage(),
                job.createdAt(),
                job.startedAt(),
                job.finishedAt()
        );
    }

    private SurveyAiSummaryViewResult completedView(Integer surveyId, LoadSurveyAiSummaryPort.SurveyAiSummaryRecord summary) {
        return new SurveyAiSummaryViewResult(
                surveyId,
                SurveyAiSummaryJobStatus.COMPLETED.name(),
                null,
                summary.commentCount(),
                summary.summaryText(),
                summary.highlights(),
                summary.concerns(),
                summary.actions(),
                null,
                summary.createdAt(),
                summary.createdAt(),
                summary.createdAt()
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

    private String errorMessage(Exception exception) {
        String message = exception.getMessage();
        return exception.getClass().getSimpleName() + (message == null || message.isBlank() ? "" : ": " + message);
    }
}
