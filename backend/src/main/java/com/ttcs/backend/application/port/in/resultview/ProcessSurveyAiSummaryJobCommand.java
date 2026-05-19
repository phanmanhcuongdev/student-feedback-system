package com.ttcs.backend.application.port.in.resultview;

import com.ttcs.backend.application.domain.model.Survey;
import com.ttcs.backend.application.port.out.LoadSurveyAiSummaryPort;

public record ProcessSurveyAiSummaryJobCommand(
        Integer jobId,
        Survey survey,
        LoadSurveyAiSummaryPort.SurveyAiSummaryPayload payload,
        String sourceHash,
        Integer expectedSourceVersion,
        Integer requestedByUserId
) {
}
