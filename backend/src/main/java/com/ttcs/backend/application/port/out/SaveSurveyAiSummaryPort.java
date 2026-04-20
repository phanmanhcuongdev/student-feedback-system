package com.ttcs.backend.application.port.out;

import java.time.LocalDateTime;
import java.util.List;

public interface SaveSurveyAiSummaryPort {
    LoadSurveyAiSummaryPort.SurveyAiSummaryJobRecord createJob(Integer surveyId, String sourceHash, Integer commentCount, Integer requestedByUserId);

    void markJobProcessing(Integer jobId, LocalDateTime startedAt);

    void markJobCompleted(Integer jobId, Integer summaryId, LocalDateTime finishedAt);

    void markJobFailed(Integer jobId, String errorMessage, LocalDateTime finishedAt);

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
}
