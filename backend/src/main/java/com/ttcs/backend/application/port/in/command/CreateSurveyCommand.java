package com.ttcs.backend.application.port.in.command;

import com.ttcs.backend.application.domain.model.SurveyRecipientScope;
import com.ttcs.backend.application.domain.model.QuestionType;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;
import java.util.List;

public record CreateSurveyCommand(
        String title,
        @Nullable String description,
        @Nullable LocalDateTime startDate,
        @Nullable LocalDateTime endDate,
        Integer createdBy,
        List<CreateQuestionCommand> questions,
        SurveyRecipientScope recipientScope,
        @Nullable Integer recipientDepartmentId,
        @Nullable String targetLang
) {
    public CreateSurveyCommand(
            String title,
            @Nullable String description,
            @Nullable LocalDateTime startDate,
            @Nullable LocalDateTime endDate,
            Integer createdBy,
            List<CreateQuestionCommand> questions,
            SurveyRecipientScope recipientScope,
            @Nullable Integer recipientDepartmentId
    ) {
        this(title, description, startDate, endDate, createdBy, questions, recipientScope, recipientDepartmentId, null);
    }
}
