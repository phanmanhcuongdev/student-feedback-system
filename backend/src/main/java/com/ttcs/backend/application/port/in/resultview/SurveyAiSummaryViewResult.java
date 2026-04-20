package com.ttcs.backend.application.port.in.resultview;

import java.time.LocalDateTime;
import java.util.List;

public record SurveyAiSummaryViewResult(
        Integer surveyId,
        String status,
        Integer jobId,
        Integer commentCount,
        String summary,
        List<String> highlights,
        List<String> concerns,
        List<String> actions,
        String modelName,
        String errorMessage,
        LocalDateTime requestedAt,
        LocalDateTime startedAt,
        LocalDateTime finishedAt
) {
}
