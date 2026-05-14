package com.ttcs.backend.application.port.out;

import com.ttcs.backend.application.port.in.resultview.ProcessSurveyAiSummaryJobCommand;

public interface ScheduleSurveyAiSummaryJobPort {
    void schedule(ProcessSurveyAiSummaryJobCommand command);
}
