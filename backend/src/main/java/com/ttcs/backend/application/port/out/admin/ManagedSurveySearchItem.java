package com.ttcs.backend.application.port.out.admin;

import java.time.LocalDateTime;

public record ManagedSurveySearchItem(
        Integer id,
        String title,
        String description,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String lifecycleState,
        String runtimeStatus,
        boolean hidden,
        String recipientScope,
        Integer recipientDepartmentId,
        String recipientDepartmentName,
        long responseCount,
        long targetedCount,
        long openedCount,
        long submittedCount,
        double responseRate
) {
}
