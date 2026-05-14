package com.ttcs.backend.application.port.in.resultview;

import com.ttcs.backend.application.domain.model.ResponseDetail;

import java.util.List;

public interface RecordSurveyAiSummaryChangeUseCase {
    void recordSubmittedTextComments(List<ResponseDetail> responseDetails);
}
