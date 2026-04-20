package com.ttcs.backend.application.port.out;

import java.time.LocalDateTime;
import java.util.List;

public record SurveyReport(
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
        List<SurveyReportQuestion> questions
) {
}
