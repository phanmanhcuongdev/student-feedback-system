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
import com.ttcs.backend.application.port.in.resultview.ProcessSurveyAiSummaryJobCommand;
import com.ttcs.backend.application.port.in.resultview.ProcessSurveyAiSummaryJobUseCase;
import com.ttcs.backend.application.port.in.resultview.SurveyAiSummaryViewResult;
import com.ttcs.backend.application.port.out.GenerateSurveyCommentSummaryPort;
import com.ttcs.backend.application.port.out.GenerateTextEmbeddingPort;
import com.ttcs.backend.application.port.out.LoadSurveyAiSummaryPort;
import com.ttcs.backend.application.port.out.LoadSurveyAssignmentPort;
import com.ttcs.backend.application.port.out.LoadLecturerByUserIdPort;
import com.ttcs.backend.application.port.out.LoadSurveyPort;
import com.ttcs.backend.application.port.out.ScheduleSurveyAiSummaryJobPort;
import com.ttcs.backend.application.port.out.SaveSurveyAiSummaryPort;
import com.ttcs.backend.application.port.out.SurveyCommentSummaryCommand;
import com.ttcs.backend.application.port.out.SurveyCommentSummaryResult;
import com.ttcs.backend.common.UseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.FORBIDDEN;

@UseCase
@RequiredArgsConstructor
@Slf4j
public class SurveyAiSummaryService implements GenerateSurveyAiSummaryUseCase, GetSurveyAiSummaryUseCase, ProcessSurveyAiSummaryJobUseCase {

    private static final double REFRESH_PENDING_RATIO_THRESHOLD = 0.05d;
    private static final int REFRESH_PENDING_SCORE_THRESHOLD = 15;
    private static final int REFRESH_MAX_SINGLE_SCORE_THRESHOLD = 7;
    private static final double REFRESH_ENTROPY_DELTA_THRESHOLD = 0.15d;
    private static final int REFRESH_NEW_IMPORTANT_TOPIC_THRESHOLD = 3;

    private final LoadSurveyPort loadSurveyPort;
    private final LoadSurveyAssignmentPort loadSurveyAssignmentPort;
    private final LoadLecturerByUserIdPort loadLecturerByUserIdPort;
    private final LoadSurveyAiSummaryPort loadSurveyAiSummaryPort;
    private final GenerateSurveyCommentSummaryPort generateSurveyCommentSummaryPort;
    private final GenerateTextEmbeddingPort generateTextEmbeddingPort;
    private final SaveSurveyAiSummaryPort saveSurveyAiSummaryPort;
    private final ScheduleSurveyAiSummaryJobPort scheduleSurveyAiSummaryJobPort;
    private final SurveyAiSummaryChangeScorer changeScorer;

    @Override
    public SurveyAiSummaryViewResult getSummary(Integer surveyId, Integer viewerUserId, Role viewerRole) {
        authorizeAccess(surveyId, viewerUserId, viewerRole);

        var latestJob = loadSurveyAiSummaryPort.loadLatestJob(surveyId).orElse(null);
        var latestSummary = resolveSummary(surveyId, latestJob);
        var sourceState = loadOrRebuildSourceState(surveyId, latestSummary);
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

        SourceSnapshot sourceSnapshot = loadSourceSnapshot(surveyId, latestSummary, sourceState);
        sourceState = sourceSnapshot.sourceState();
        var payload = sourceSnapshot.payload();
        int expectedSourceVersion = sourceState != null ? sourceState.sourceVersion() : 0;
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
            scheduleSurveyAiSummaryJobPort.schedule(new ProcessSurveyAiSummaryJobCommand(
                    job.id(),
                    survey,
                    payload,
                    sourceHash,
                    expectedSourceVersion,
                    viewerUserId
            ));
        } catch (Exception exception) {
            saveSurveyAiSummaryPort.markJobFailed(job.id(), errorMessage(exception), LocalDateTime.now());
        }
        return toView(surveyId, job, null, sourceState);
    }

    @Override
    public void processJob(ProcessSurveyAiSummaryJobCommand command) {
        Integer jobId = command.jobId();
        Survey survey = command.survey();
        LoadSurveyAiSummaryPort.SurveyAiSummaryPayload payload = command.payload();
        String sourceHash = command.sourceHash();
        Integer expectedSourceVersion = command.expectedSourceVersion();
        Integer viewerUserId = command.requestedByUserId();
        try {
            boolean claimed = saveSurveyAiSummaryPort.markJobProcessingIfNoActiveJob(jobId, survey.getId(), LocalDateTime.now());
            if (!claimed) {
                String message = "Another AI summary job is already queued or processing for surveyId=" + survey.getId();
                log.warn("Skip AI summary jobId={} for surveyId={}: {}", jobId, survey.getId(), message);
                saveSurveyAiSummaryPort.markJobFailed(jobId, message, LocalDateTime.now());
                return;
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
            saveThemeEmbeddings(summary);
        } catch (Exception exception) {
            saveSurveyAiSummaryPort.markJobFailed(jobId, errorMessage(exception), LocalDateTime.now());
        }
    }

    private LoadSurveyAiSummaryPort.SurveyAiSummarySourceStateRecord loadOrRebuildSourceState(
            Integer surveyId,
            LoadSurveyAiSummaryPort.SurveyAiSummaryRecord latestSummary
    ) {
        var sourceState = loadSurveyAiSummaryPort.loadSourceState(surveyId).orElse(null);
        if (sourceState != null) {
            return sourceState;
        }
        var payload = loadSurveyAiSummaryPort.loadSurveySummaryPayload(surveyId);
        rebuildSourceState(surveyId, latestSummary, payload);
        return loadSurveyAiSummaryPort.loadSourceState(surveyId)
                .orElseGet(() -> fallbackRebuiltSourceState(surveyId, latestSummary, payload));
    }

    private SourceSnapshot loadSourceSnapshot(
            Integer surveyId,
            LoadSurveyAiSummaryPort.SurveyAiSummaryRecord latestSummary,
            LoadSurveyAiSummaryPort.SurveyAiSummarySourceStateRecord knownSourceState
    ) {
        if (knownSourceState == null) {
            var payload = loadSurveyAiSummaryPort.loadSurveySummaryPayload(surveyId);
            rebuildSourceState(surveyId, latestSummary, payload);
            var rebuiltState = loadSurveyAiSummaryPort.loadSourceState(surveyId)
                    .orElseGet(() -> fallbackRebuiltSourceState(surveyId, latestSummary, payload));
            return new SourceSnapshot(payload, rebuiltState);
        }

        var before = knownSourceState;
        var payload = loadSurveyAiSummaryPort.loadSurveySummaryPayload(surveyId);
        var after = loadSurveyAiSummaryPort.loadSourceState(surveyId).orElse(before);
        if (!sameVersion(before, after)) {
            payload = loadSurveyAiSummaryPort.loadSurveySummaryPayload(surveyId);
            after = loadSurveyAiSummaryPort.loadSourceState(surveyId).orElse(after);
        }
        return new SourceSnapshot(payload, after);
    }

    private void rebuildSourceState(Integer surveyId,
                                    LoadSurveyAiSummaryPort.SurveyAiSummaryRecord latestSummary,
                                    LoadSurveyAiSummaryPort.SurveyAiSummaryPayload payload) {
        saveSurveyAiSummaryPort.rebuildSourceState(new SaveSurveyAiSummaryPort.SurveyAiSummarySourceStateRebuildCommand(
                surveyId,
                payload.commentCount(),
                latestSummary != null ? latestSummary.commentCount() : 0,
                topicCounts(payload),
                latestSummary != null ? latestSummary.createdAt() : null,
                LocalDateTime.now()
        ));
    }

    private LoadSurveyAiSummaryPort.SurveyAiSummarySourceStateRecord fallbackRebuiltSourceState(
            Integer surveyId,
            LoadSurveyAiSummaryPort.SurveyAiSummaryRecord latestSummary,
            LoadSurveyAiSummaryPort.SurveyAiSummaryPayload payload
    ) {
        int currentCommentCount = payload.commentCount();
        int summarizedCommentCount = latestSummary == null ? 0 : Math.min(latestSummary.commentCount(), currentCommentCount);
        int pendingCommentCount = Math.max(0, currentCommentCount - summarizedCommentCount);
        return new LoadSurveyAiSummaryPort.SurveyAiSummarySourceStateRecord(
                surveyId,
                currentCommentCount,
                summarizedCommentCount,
                pendingCommentCount,
                0,
                0,
                "{}",
                "{}",
                0.0d,
                0.0d,
                0,
                currentCommentCount,
                summarizedCommentCount,
                pendingCommentCount > 0 ? LocalDateTime.now() : null,
                latestSummary != null ? latestSummary.createdAt() : null
        );
    }

    private boolean sameVersion(LoadSurveyAiSummaryPort.SurveyAiSummarySourceStateRecord left,
                                LoadSurveyAiSummaryPort.SurveyAiSummarySourceStateRecord right) {
        if (left == null || right == null) {
            return left == right;
        }
        return left.sourceVersion().equals(right.sourceVersion())
                && left.pendingCommentCount().equals(right.pendingCommentCount());
    }

    private void saveThemeEmbeddings(LoadSurveyAiSummaryPort.SurveyAiSummaryRecord summary) {
        try {
            List<ThemeText> themeTexts = themeTexts(summary);
            if (themeTexts.isEmpty()) {
                return;
            }
            var embeddingResult = generateTextEmbeddingPort.embed(themeTexts.stream().map(ThemeText::text).toList());
            if (embeddingResult.vectors().isEmpty()) {
                return;
            }
            if (embeddingResult.vectors().size() != themeTexts.size()) {
                log.warn("Skip AI summary theme embeddings for summaryId={} because embedding count does not match theme count", summary.id());
                return;
            }
            List<SaveSurveyAiSummaryPort.SurveyAiSummaryThemeEmbeddingItem> items = new ArrayList<>();
            for (int index = 0; index < themeTexts.size(); index++) {
                List<Double> vector = embeddingResult.vectors().get(index);
                if (vector == null || vector.isEmpty()) {
                    continue;
                }
                ThemeText themeText = themeTexts.get(index);
                items.add(new SaveSurveyAiSummaryPort.SurveyAiSummaryThemeEmbeddingItem(
                        themeText.type(),
                        themeText.index(),
                        themeText.text(),
                        vector
                ));
            }
            saveSurveyAiSummaryPort.saveThemeEmbeddings(new SaveSurveyAiSummaryPort.SurveyAiSummaryThemeEmbeddingSaveCommand(
                    summary.id(),
                    summary.surveyId(),
                    embeddingResult.modelName(),
                    items
            ));
        } catch (Exception exception) {
            log.warn("Skip AI summary theme embeddings for summaryId={} because embedding failed: {}", summary.id(), exception.getMessage());
        }
    }

    private List<ThemeText> themeTexts(LoadSurveyAiSummaryPort.SurveyAiSummaryRecord summary) {
        List<ThemeText> texts = new ArrayList<>();
        addThemeText(texts, "SUMMARY", 0, summary.summaryText());
        addThemeTexts(texts, "HIGHLIGHT", summary.highlights());
        addThemeTexts(texts, "CONCERN", summary.concerns());
        addThemeTexts(texts, "ACTION", summary.actions());
        return texts;
    }

    private void addThemeTexts(List<ThemeText> target, String type, List<String> values) {
        if (values == null) {
            return;
        }
        for (int index = 0; index < values.size(); index++) {
            addThemeText(target, type, index, values.get(index));
        }
    }

    private void addThemeText(List<ThemeText> target, String type, int index, String value) {
        if (value != null && !value.isBlank()) {
            target.add(new ThemeText(type, index, value.strip()));
        }
    }

    private record ThemeText(
            String type,
            int index,
            String text
    ) {
    }

    private record SourceSnapshot(
            LoadSurveyAiSummaryPort.SurveyAiSummaryPayload payload,
            LoadSurveyAiSummaryPort.SurveyAiSummarySourceStateRecord sourceState
    ) {
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
        int importantTopicCount = sourceState.importantPendingTopicCount();

        boolean recommended = pendingRatio >= REFRESH_PENDING_RATIO_THRESHOLD
                || pendingScore >= REFRESH_PENDING_SCORE_THRESHOLD
                || maxPendingScore >= REFRESH_MAX_SINGLE_SCORE_THRESHOLD
                || entropyDelta >= REFRESH_ENTROPY_DELTA_THRESHOLD
                || importantTopicCount >= REFRESH_NEW_IMPORTANT_TOPIC_THRESHOLD;

        String reason = changeReason(recommended, pendingCount, pendingScore, maxPendingScore, pendingRatio, entropyDelta, importantTopicCount);
        return new ChangeDecision(true, recommended, pendingCount, pendingScore, maxPendingScore, pendingRatio, entropyDelta, reason);
    }

    private String changeReason(boolean recommended,
                                int pendingCount,
                                int pendingScore,
                                int maxPendingScore,
                                double pendingRatio,
                                double entropyDelta,
                                int importantTopicCount) {
        if (!recommended) {
            return "There are " + pendingCount + " new text feedback item(s), but they are below the refresh thresholds.";
        }
        if (pendingRatio >= REFRESH_PENDING_RATIO_THRESHOLD) {
            return "New text feedback is " + formatPercent(pendingRatio) + " of the summarized source, meeting the refresh threshold.";
        }
        if (pendingScore >= REFRESH_PENDING_SCORE_THRESHOLD) {
            return "New text feedback has a pending score of " + pendingScore + ", meeting the refresh threshold.";
        }
        if (maxPendingScore >= REFRESH_MAX_SINGLE_SCORE_THRESHOLD) {
            return "At least one new text feedback item has a high score of " + maxPendingScore + ".";
        }
        if (importantTopicCount >= REFRESH_NEW_IMPORTANT_TOPIC_THRESHOLD) {
            return "A new important feedback topic appeared " + importantTopicCount + " time(s), meeting the refresh threshold.";
        }
        return "New text feedback changed the topic distribution enough to refresh the AI summary.";
    }

    private String formatPercent(double ratio) {
        return "%.1f%%".formatted(ratio * 100.0d);
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
