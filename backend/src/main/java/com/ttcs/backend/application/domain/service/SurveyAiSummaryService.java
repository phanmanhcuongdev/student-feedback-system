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
import java.util.LinkedHashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.springframework.http.HttpStatus.FORBIDDEN;

@UseCase
@RequiredArgsConstructor
@Slf4j
public class SurveyAiSummaryService implements GenerateSurveyAiSummaryUseCase, GetSurveyAiSummaryUseCase {

    private static final int REFRESH_PENDING_COUNT_THRESHOLD = 10;
    private static final double REFRESH_PENDING_RATIO_THRESHOLD = 0.05d;
    private static final int REFRESH_PENDING_SCORE_THRESHOLD = 15;
    private static final int REFRESH_MAX_SINGLE_SCORE_THRESHOLD = 7;
    private static final double REFRESH_ENTROPY_DELTA_THRESHOLD = 0.15d;

    private final LoadSurveyPort loadSurveyPort;
    private final LoadSurveyAssignmentPort loadSurveyAssignmentPort;
    private final LoadLecturerByUserIdPort loadLecturerByUserIdPort;
    private final LoadSurveyAiSummaryPort loadSurveyAiSummaryPort;
    private final GenerateSurveyCommentSummaryPort generateSurveyCommentSummaryPort;
    private final SaveSurveyAiSummaryPort saveSurveyAiSummaryPort;
    private final ObjectProvider<SurveyAiSummaryService> selfProvider;
    private final SurveyAiSummaryChangeScorer changeScorer;

    @Override
    public SurveyAiSummaryViewResult getSummary(Integer surveyId, Integer viewerUserId, Role viewerRole) {
        authorizeAccess(surveyId, viewerUserId, viewerRole);

        var latestJob = loadSurveyAiSummaryPort.loadLatestJob(surveyId).orElse(null);
        var latestSummary = resolveSummary(surveyId, latestJob);
        var sourceState = loadSurveyAiSummaryPort.loadSourceState(surveyId).orElse(null);
        if (latestJob != null) {
            return toView(surveyId, latestJob, latestSummary, sourceState);
        }
        if (latestSummary != null) {
            return completedView(surveyId, latestSummary, sourceState);
        }
        return new SurveyAiSummaryViewResult(
                surveyId,
                "NOT_REQUESTED",
                null,
                0,
                null,
                List.of(),
                List.of(),
                List.of(),
                null,
                null,
                null,
                null,
                false,
                true,
                sourceState != null ? sourceState.pendingCommentCount() : 0,
                sourceState != null ? sourceState.pendingScoreSum() : 0,
                sourceState != null ? sourceState.maxPendingScore() : 0,
                sourceState != null ? sourceState.pendingRatio() : 0.0d,
                sourceState != null ? sourceState.entropyDelta() : 0.0d,
                "No AI summary has been generated yet.",
                sourceState != null ? sourceState.lastChangedAt() : null,
                sourceState != null ? sourceState.lastSummarizedAt() : null
        );
    }

    @Override
    public SurveyAiSummaryViewResult generate(Integer surveyId, Integer viewerUserId, Role viewerRole) {
        Survey survey = authorizeAccess(surveyId, viewerUserId, viewerRole);
        var latestSummary = loadSurveyAiSummaryPort.loadLatestSummary(surveyId).orElse(null);
        var sourceState = loadSurveyAiSummaryPort.loadSourceState(surveyId).orElse(null);
        if (latestSummary != null && sourceState != null && !refreshRecommended(sourceState)) {
            return completedView(surveyId, latestSummary, sourceState);
        }
        int expectedSourceVersion = sourceState != null ? sourceState.sourceVersion() : 0;

        var payload = loadSurveyAiSummaryPort.loadSurveySummaryPayload(surveyId);
        String sourceHash = computeSourceHash(payload);

        if (latestSummary != null && sourceHash.equals(latestSummary.sourceHash())) {
            saveSurveyAiSummaryPort.markSourceStateSummarized(
                    surveyId,
                    payload.commentCount(),
                    topicCounts(payload),
                    expectedSourceVersion,
                    LocalDateTime.now()
            );
            var refreshedState = loadSurveyAiSummaryPort.loadSourceState(surveyId).orElse(sourceState);
            return completedView(surveyId, latestSummary, refreshedState);
        }

        var latestJob = loadSurveyAiSummaryPort.loadLatestJob(surveyId).orElse(null);
        if (latestJob != null
                && sourceHash.equals(latestJob.sourceHash())
                && (latestJob.status() == SurveyAiSummaryJobStatus.QUEUED || latestJob.status() == SurveyAiSummaryJobStatus.PROCESSING)) {
            return toView(surveyId, latestJob, null, sourceState);
        }

        var activeJob = loadSurveyAiSummaryPort.loadActiveJob(surveyId).orElse(null);
        if (activeJob != null) {
            log.warn(
                    "Skip AI summary generation for surveyId={} because jobId={} is already {}",
                    surveyId,
                    activeJob.id(),
                    activeJob.status()
            );
            return toView(surveyId, activeJob, null, sourceState);
        }

        if (payload.commentCount() == 0) {
            LocalDateTime now = LocalDateTime.now();
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
            saveSurveyAiSummaryPort.markSourceStateSummarized(
                    surveyId,
                    payload.commentCount(),
                    topicCounts(payload),
                    expectedSourceVersion,
                    now
            );
            var refreshedState = loadSurveyAiSummaryPort.loadSourceState(surveyId).orElse(sourceState);
            return completedView(surveyId, summary, refreshedState);
        }

        var job = saveSurveyAiSummaryPort.createJob(surveyId, sourceHash, payload.commentCount(), viewerUserId);
        try {
            selfProvider.getObject().processJob(job.id(), survey, payload, sourceHash, expectedSourceVersion, viewerUserId);
        } catch (Exception exception) {
            saveSurveyAiSummaryPort.markJobFailed(job.id(), errorMessage(exception), LocalDateTime.now());
        }
        return toView(surveyId, job, null, sourceState);
    }

    @Async("aiTaskExecutor")
    public CompletableFuture<Void> processJob(Integer jobId,
                                              Survey survey,
                                              LoadSurveyAiSummaryPort.SurveyAiSummaryPayload payload,
                                              String sourceHash,
                                              Integer expectedSourceVersion,
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
            LocalDateTime finishedAt = LocalDateTime.now();
            saveSurveyAiSummaryPort.markJobCompleted(jobId, summary.id(), finishedAt);
            saveSurveyAiSummaryPort.markSourceStateSummarized(
                    survey.getId(),
                    payload.commentCount(),
                    topicCounts(payload),
                    expectedSourceVersion,
                    finishedAt
            );
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
                                             LoadSurveyAiSummaryPort.SurveyAiSummaryRecord summary,
                                             LoadSurveyAiSummaryPort.SurveyAiSummarySourceStateRecord sourceState) {
        ChangeDecision decision = changeDecision(sourceState);
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
                job.finishedAt(),
                decision.stale(),
                decision.refreshRecommended(),
                decision.pendingCommentCount(),
                decision.pendingScoreSum(),
                decision.maxPendingScore(),
                decision.pendingRatio(),
                decision.entropyDelta(),
                decision.reason(),
                sourceState != null ? sourceState.lastChangedAt() : null,
                sourceState != null ? sourceState.lastSummarizedAt() : null
        );
    }

    private SurveyAiSummaryViewResult completedView(Integer surveyId,
                                                    LoadSurveyAiSummaryPort.SurveyAiSummaryRecord summary,
                                                    LoadSurveyAiSummaryPort.SurveyAiSummarySourceStateRecord sourceState) {
        ChangeDecision decision = changeDecision(sourceState);
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
                summary.createdAt(),
                decision.stale(),
                decision.refreshRecommended(),
                decision.pendingCommentCount(),
                decision.pendingScoreSum(),
                decision.maxPendingScore(),
                decision.pendingRatio(),
                decision.entropyDelta(),
                decision.reason(),
                sourceState != null ? sourceState.lastChangedAt() : null,
                sourceState != null ? sourceState.lastSummarizedAt() : summary.createdAt()
        );
    }

    private boolean refreshRecommended(LoadSurveyAiSummaryPort.SurveyAiSummarySourceStateRecord sourceState) {
        return changeDecision(sourceState).refreshRecommended();
    }

    private ChangeDecision changeDecision(LoadSurveyAiSummaryPort.SurveyAiSummarySourceStateRecord sourceState) {
        if (sourceState == null || !sourceState.hasPendingChanges()) {
            return new ChangeDecision(false, false, 0, 0, 0, 0.0d, 0.0d, "Summary source has no pending text feedback changes.");
        }

        int pendingCount = sourceState.pendingCommentCount();
        int pendingScore = sourceState.pendingScoreSum();
        int maxPendingScore = sourceState.maxPendingScore();
        double pendingRatio = sourceState.pendingRatio();
        double entropyDelta = sourceState.entropyDelta();

        boolean recommended = pendingCount >= REFRESH_PENDING_COUNT_THRESHOLD
                || pendingRatio >= REFRESH_PENDING_RATIO_THRESHOLD
                || pendingScore >= REFRESH_PENDING_SCORE_THRESHOLD
                || maxPendingScore >= REFRESH_MAX_SINGLE_SCORE_THRESHOLD
                || entropyDelta >= REFRESH_ENTROPY_DELTA_THRESHOLD;

        String reason = recommended
                ? "Pending feedback changes are large or meaningful enough to refresh the AI summary."
                : "Pending feedback changes are minor, so the current AI summary can still be reused.";
        return new ChangeDecision(true, recommended, pendingCount, pendingScore, maxPendingScore, pendingRatio, entropyDelta, reason);
    }

    private record ChangeDecision(
            boolean stale,
            boolean refreshRecommended,
            int pendingCommentCount,
            int pendingScoreSum,
            int maxPendingScore,
            double pendingRatio,
            double entropyDelta,
            String reason
    ) {
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

    private Map<String, Integer> topicCounts(LoadSurveyAiSummaryPort.SurveyAiSummaryPayload payload) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        if (payload == null || payload.comments() == null) {
            return counts;
        }
        for (LoadSurveyAiSummaryPort.QuestionCommentPayload comment : payload.comments()) {
            String topic = changeScorer.classifyTopic(comment.comment());
            counts.merge(topic, 1, Integer::sum);
        }
        return counts;
    }

    private String errorMessage(Exception exception) {
        String message = exception.getMessage();
        return exception.getClass().getSimpleName() + (message == null || message.isBlank() ? "" : ": " + message);
    }
}
