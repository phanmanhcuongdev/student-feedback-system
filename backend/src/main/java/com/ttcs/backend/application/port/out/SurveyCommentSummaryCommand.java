package com.ttcs.backend.application.port.out;

import java.util.List;

public record SurveyCommentSummaryCommand(
        Integer surveyId,
        String surveyTitle,
        Integer commentCount,
        List<String> commentEntries
) {
}
