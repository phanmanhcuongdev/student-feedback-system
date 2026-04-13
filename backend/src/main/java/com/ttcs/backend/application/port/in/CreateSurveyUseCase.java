package com.ttcs.backend.application.port.in;

import com.ttcs.backend.application.port.in.command.CreateSurveyCommand;

public interface CreateSurveyUseCase {
    Integer createSurvey(CreateSurveyCommand command);
}
