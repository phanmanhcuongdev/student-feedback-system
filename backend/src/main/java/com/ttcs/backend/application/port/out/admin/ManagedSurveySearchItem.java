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
        com.ttcs.backend.application.domain.model.SubjectType subjectType,
        Integer subjectValue,
        String subjectName,
        long responseCount,
        long targetedCount,
        long openedCount,
        long submittedCount,
        double responseRate
) {
}
