package com.ttcs.backend.adapter.in.web.dto;

import java.time.LocalDateTime;
import java.util.List;

public record SurveyManagementDetailResponse(
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
        List<SurveyManagementQuestionResponse> questions,
        List<SurveyManagementRecipientResponse> pendingRecipients
) {
}
