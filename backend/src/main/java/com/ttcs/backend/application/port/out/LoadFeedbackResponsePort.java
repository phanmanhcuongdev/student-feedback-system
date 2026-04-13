package com.ttcs.backend.application.port.out;

import com.ttcs.backend.application.domain.model.FeedbackResponse;

import java.util.List;

public interface LoadFeedbackResponsePort {
    List<FeedbackResponse> loadByFeedbackIds(List<Integer> feedbackIds);

    List<FeedbackResponse> loadByFeedbackId(Integer feedbackId);
}
