package com.ttcs.backend.application.port.in.admin;

import com.ttcs.backend.application.domain.model.SurveyRecipientScope;
import com.ttcs.backend.application.domain.model.SubjectType;

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
        Integer recipientDepartmentId,
        List<Integer> recipientStudentIds,
        SubjectType subjectType,
        Integer subjectValue,
        String subjectName,
        String targetLang
) {
    public UpdateSurveyCommand(
            Integer surveyId,
            String title,
            String description,
            LocalDateTime startDate,
            LocalDateTime endDate,
            List<UpdateSurveyQuestionCommand> questions,
            SurveyRecipientScope recipientScope,
            Integer recipientDepartmentId,
            SubjectType subjectType,
            Integer subjectValue,
            String targetLang
    ) {
        this(surveyId, title, description, startDate, endDate, questions, recipientScope, recipientDepartmentId, null, subjectType, subjectValue, null, targetLang);
    }

    public UpdateSurveyCommand(
            Integer surveyId,
            String title,
            String description,
            LocalDateTime startDate,
            LocalDateTime endDate,
            List<UpdateSurveyQuestionCommand> questions,
            SurveyRecipientScope recipientScope,
            Integer recipientDepartmentId,
            SubjectType subjectType,
            Integer subjectValue,
            String subjectName,
            String targetLang
    ) {
        this(surveyId, title, description, startDate, endDate, questions, recipientScope, recipientDepartmentId, null, subjectType, subjectValue, subjectName, targetLang);
    }
}
