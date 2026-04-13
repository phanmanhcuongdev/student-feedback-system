package com.ttcs.backend.application.port.out;

import com.ttcs.backend.application.domain.model.Feedback;

public interface SaveFeedbackPort {
    Feedback save(Feedback feedback);
}
