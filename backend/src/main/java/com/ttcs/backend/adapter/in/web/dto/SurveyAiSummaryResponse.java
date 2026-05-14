package com.ttcs.backend.adapter.in.web.dto;

import java.time.LocalDateTime;
import java.util.List;

public record SurveyAiSummaryResponse(
        Integer surveyId,
        String status,
        Integer jobId,
        Integer commentCount,
        String summary,
        List<String> highlights,
        List<String> concerns,
        List<String> actions,
        String errorMessage,
        LocalDateTime requestedAt,
        LocalDateTime startedAt,
        LocalDateTime finishedAt,
        Boolean stale,
        Boolean isStale,
        Boolean refreshRecommended,
        Integer pendingCommentCount,
        Integer pendingScoreSum,
        Integer maxPendingScore,
        Double pendingRatio,
        Double entropyDelta,
        String changeReason,
        LocalDateTime lastChangedAt,
        LocalDateTime lastSummarizedAt
) {
}
