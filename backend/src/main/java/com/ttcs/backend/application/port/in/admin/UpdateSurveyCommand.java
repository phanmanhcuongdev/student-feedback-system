package com.ttcs.backend.application.port.in.admin;

import com.ttcs.backend.application.domain.model.SurveyRecipientScope;

import java.time.LocalDateTime;
import java.util.List;

public record UpdateSurveyCommand(
        Integer surveyId,
        String title,
        String description,
        LocalDateTime startDate,
        LocalDateTime endDate,
        List<UpdateSurveyQuestionCommand> questions,
        SurveyRecipientScope recipientScope,
        Integer recipientDepartmentId
) {
}
