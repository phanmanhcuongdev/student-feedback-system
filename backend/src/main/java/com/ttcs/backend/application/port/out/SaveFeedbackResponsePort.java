package com.ttcs.backend.application.port.out;

import com.ttcs.backend.application.domain.model.FeedbackResponse;

public interface SaveFeedbackResponsePort {
    FeedbackResponse save(FeedbackResponse response);
}
