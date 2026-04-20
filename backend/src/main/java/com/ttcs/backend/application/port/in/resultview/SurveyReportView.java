package com.ttcs.backend.application.port.in.resultview;

import java.time.LocalDateTime;
import java.util.List;

public record SurveyReportView(
        Integer id,
        String title,
        String description,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String lifecycleState,
        String runtimeStatus,
        String recipientScope,
        String recipientDepartmentName,
        long targetedCount,
        long openedCount,
        long submittedCount,
        double responseRate,
        List<SurveyReportQuestionView> questions
) {
}
