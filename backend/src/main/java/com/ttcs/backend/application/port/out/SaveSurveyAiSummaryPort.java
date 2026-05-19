package com.ttcs.backend.application.port.out;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface SaveSurveyAiSummaryPort {
    LoadSurveyAiSummaryPort.SurveyAiSummaryJobRecord createJob(Integer surveyId, String sourceHash, Integer commentCount, Integer requestedByUserId);

    void markJobProcessing(Integer jobId, LocalDateTime startedAt);

    boolean markJobProcessingIfNoActiveJob(Integer jobId, Integer surveyId, LocalDateTime startedAt);

    void markJobCompleted(Integer jobId, Integer summaryId, LocalDateTime finishedAt);

    void markJobFailed(Integer jobId, String errorMessage, LocalDateTime finishedAt);

    void recordTextCommentChange(SurveyAiSummaryTextChangeCommand command);

    void rebuildSourceState(SurveyAiSummarySourceStateRebuildCommand command);

    void saveThemeEmbeddings(SurveyAiSummaryThemeEmbeddingSaveCommand command);

    void markSourceStateSummarized(Integer surveyId,
                                   Integer commentCount,
                                   Map<String, Integer> topicCounts,
                                   Integer expectedSourceVersion,
                                   LocalDateTime summarizedAt);

    LoadSurveyAiSummaryPort.SurveyAiSummaryRecord saveSummary(
            Integer surveyId,
            String sourceHash,
            String modelName,
            Integer commentCount,
            String summaryText,
            List<String> highlights,
            List<String> concerns,
            List<String> actions,
            Integer createdByUserId
    );

    record SurveyAiSummaryTextChangeCommand(
            Integer surveyId,
            Integer responseDetailId,
            Integer questionId,
            Integer commentLength,
            String topic,
            Integer keywordScore,
            Integer sentimentScore,
            Integer suggestionScore,
            Integer entropyImpactScore,
            Integer noveltyScore,
            Integer totalScore,
            LocalDateTime createdAt
    ) {
    }

    record SurveyAiSummarySourceStateRebuildCommand(
            Integer surveyId,
            Integer currentCommentCount,
            Integer summarizedCommentCount,
            Map<String, Integer> topicCounts,
            LocalDateTime lastSummarizedAt,
            LocalDateTime rebuiltAt
    ) {
    }

    record SurveyAiSummaryThemeEmbeddingSaveCommand(
            Integer summaryId,
            Integer surveyId,
            String modelName,
            List<SurveyAiSummaryThemeEmbeddingItem> items
    ) {
    }

    record SurveyAiSummaryThemeEmbeddingItem(
            String themeType,
            Integer themeIndex,
            String themeText,
            List<Double> vector
    ) {
    }
}
