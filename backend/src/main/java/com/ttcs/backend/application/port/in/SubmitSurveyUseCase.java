package com.ttcs.backend.application.port.in;

import com.ttcs.backend.application.port.in.command.SubmitSurveyCommand;
import com.ttcs.backend.application.port.in.result.SubmitSurveyResult;

public interface SubmitSurveyUseCase {
    SubmitSurveyResult submitSurvey(SubmitSurveyCommand command);
}
