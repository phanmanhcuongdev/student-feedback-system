package com.ttcs.backend.application.port.out;

public interface GenerateSurveyCommentSummaryPort {
    SurveyCommentSummaryResult generateSummary(SurveyCommentSummaryCommand command);
}
