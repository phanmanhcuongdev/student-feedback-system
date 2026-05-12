package com.ttcs.backend.application.port.out;

import com.ttcs.backend.application.domain.model.SurveyAiSummaryJobStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LoadSurveyAiSummaryPort {
    Optional<SurveyAiSummaryRecord> loadLatestSummary(Integer surveyId);

    Optional<SurveyAiSummaryJobRecord> loadLatestJob(Integer surveyId);

    Optional<SurveyAiSummaryJobRecord> loadActiveJob(Integer surveyId);

    Optional<SurveyAiSummaryRecord> loadSummaryById(Integer summaryId);

    Optional<SurveyAiSummarySourceStateRecord> loadSourceState(Integer surveyId);

    SurveyAiSummaryPayload loadSurveySummaryPayload(Integer surveyId);

    record SurveyAiSummaryRecord(
            Integer id,
            Integer surveyId,
            String sourceHash,
            String modelName,
            Integer commentCount,
            String summaryText,
            List<String> highlights,
            List<String> concerns,
            List<String> actions,
            Integer createdByUserId,
            LocalDateTime createdAt
    ) {
    }

    record SurveyAiSummaryJobRecord(
            Integer id,
            Integer surveyId,
            String sourceHash,
            Integer commentCount,
            SurveyAiSummaryJobStatus status,
            Integer requestedByUserId,
            Integer summaryId,
            LocalDateTime createdAt,
            LocalDateTime startedAt,
            LocalDateTime finishedAt,
            String errorMessage
    ) {
    }

    record SurveyAiSummaryPayload(
            Integer surveyId,
            String surveyTitle,
            Integer commentCount,
            List<QuestionCommentPayload> comments
    ) {
    }

    record QuestionCommentPayload(
            Integer questionId,
            String questionContent,
            String comment
    ) {
    }

    record SurveyAiSummarySourceStateRecord(
            Integer surveyId,
            Integer currentCommentCount,
            Integer summarizedCommentCount,
            Integer pendingCommentCount,
            Integer pendingScoreSum,
            Integer maxPendingScore,
            String topicCountsJson,
            String pendingTopicCountsJson,
            Double currentEntropy,
            Double summarizedEntropy,
            Integer sourceVersion,
            Integer summarizedSourceVersion,
            LocalDateTime lastChangedAt,
            LocalDateTime lastSummarizedAt
    ) {
        public boolean hasPendingChanges() {
            return pendingCommentCount != null && pendingCommentCount > 0;
        }

        public double pendingRatio() {
            Integer base = summarizedCommentCount == null || summarizedCommentCount <= 0 ? currentCommentCount : summarizedCommentCount;
            if (base == null || base <= 0 || pendingCommentCount == null) {
                return 0.0d;
            }
            return (double) pendingCommentCount / (double) base;
        }

        public double entropyDelta() {
            double current = currentEntropy == null ? 0.0d : currentEntropy;
            double summarized = summarizedEntropy == null ? 0.0d : summarizedEntropy;
            return Math.abs(current - summarized);
        }
    }
}
