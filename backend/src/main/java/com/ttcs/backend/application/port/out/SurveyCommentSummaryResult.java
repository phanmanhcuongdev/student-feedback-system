package com.ttcs.backend.application.port.out;

import java.util.List;

public record SurveyCommentSummaryResult(
        String modelName,
        String summary,
        List<String> highlights,
        List<String> concerns,
        List<String> actions
) {
}
