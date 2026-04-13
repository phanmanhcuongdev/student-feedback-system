package com.ttcs.backend.adapter.out.persistence.feedbackresponse;

import com.ttcs.backend.adapter.out.persistence.feedback.FeedbackMapper;
import com.ttcs.backend.adapter.out.persistence.user.UserMapper;
import com.ttcs.backend.application.domain.model.FeedbackResponse;

public final class FeedbackResponseMapper {

    private FeedbackResponseMapper() {
    }

    public static FeedbackResponse toDomain(FeedbackResponseEntity entity) {
        return new FeedbackResponse(
                entity.getId(),
                FeedbackMapper.toDomain(entity.getFeedback()),
                UserMapper.toDomain(entity.getResponder()),
                entity.getContent(),
                entity.getCreatedAt()
        );
    }
}
