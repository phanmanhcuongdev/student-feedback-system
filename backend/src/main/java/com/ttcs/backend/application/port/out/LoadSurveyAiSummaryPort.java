package com.ttcs.backend.application.port.out;

import com.ttcs.backend.application.domain.model.SurveyAiSummaryJobStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LoadSurveyAiSummaryPort {
    Optional<SurveyAiSummaryRecord> loadLatestSummary(Integer surveyId);

    Optional<SurveyAiSummaryJobRecord> loadLatestJob(Integer surveyId);

    Optional<SurveyAiSummaryRecord> loadSummaryById(Integer summaryId);

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
}
