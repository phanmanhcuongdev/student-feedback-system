package com.ttcs.backend.adapter.out.async;

import com.ttcs.backend.application.domain.model.ResponseDetail;
import com.ttcs.backend.application.port.in.resultview.RecordSurveyAiSummaryChangeUseCase;
import com.ttcs.backend.application.port.out.ScheduleSurveyAiSummaryChangeTrackingPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AsyncSurveyAiSummaryChangeTrackingAdapter implements ScheduleSurveyAiSummaryChangeTrackingPort {

    private final RecordSurveyAiSummaryChangeUseCase recordSurveyAiSummaryChangeUseCase;

    @Override
    @Async("aiTaskExecutor")
    public void scheduleTextCommentTracking(List<ResponseDetail> responseDetails) {
        try {
            recordSurveyAiSummaryChangeUseCase.recordSubmittedTextComments(responseDetails);
        } catch (Exception exception) {
            log.warn("Skip async AI summary change tracking because recorder failed: {}", exception.getMessage());
        }
    }
}
