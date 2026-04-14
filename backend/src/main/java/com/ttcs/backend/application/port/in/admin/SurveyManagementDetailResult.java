package com.ttcs.backend.application.port.in.admin;

import java.time.LocalDateTime;
import java.util.List;

public record SurveyManagementDetailResult(
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
        long responseCount,
        long targetedCount,
        long openedCount,
        long submittedCount,
        double responseRate,
        List<SurveyManagementQuestionResult> questions,
        List<SurveyManagementRecipientResult> pendingRecipients
) {
}
