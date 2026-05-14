package com.ttcs.backend.application.port.out;

import com.ttcs.backend.application.domain.model.ResponseDetail;

import java.util.List;

public interface ScheduleSurveyAiSummaryChangeTrackingPort {
    void scheduleTextCommentTracking(List<ResponseDetail> responseDetails);
}
