package com.ttcs.backend.adapter.out.async;

import com.ttcs.backend.application.port.in.resultview.ProcessSurveyAiSummaryJobCommand;
import com.ttcs.backend.application.port.in.resultview.ProcessSurveyAiSummaryJobUseCase;
import com.ttcs.backend.application.port.out.ScheduleSurveyAiSummaryJobPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AsyncSurveyAiSummaryJobAdapter implements ScheduleSurveyAiSummaryJobPort {

    private final ObjectProvider<ProcessSurveyAiSummaryJobUseCase> processSurveyAiSummaryJobUseCase;

    @Override
    @Async("aiTaskExecutor")
    public void schedule(ProcessSurveyAiSummaryJobCommand command) {
        try {
            processSurveyAiSummaryJobUseCase.getObject().processJob(command);
        } catch (Exception exception) {
            Integer jobId = command == null ? null : command.jobId();
            log.warn("AI summary job dispatch failed for jobId={}: {}", jobId, exception.getMessage());
        }
    }
}
