package com.ttcs.backend.application.port.out;

import com.ttcs.backend.application.domain.model.SurveyRecipient;

import java.util.List;
import java.util.Optional;

public interface LoadSurveyRecipientPort {
    Optional<SurveyRecipient> loadBySurveyIdAndStudentId(Integer surveyId, Integer studentId);

    List<SurveyRecipient> loadBySurveyId(Integer surveyId);

    List<SurveyRecipient> loadByStudentId(Integer studentId);
}
