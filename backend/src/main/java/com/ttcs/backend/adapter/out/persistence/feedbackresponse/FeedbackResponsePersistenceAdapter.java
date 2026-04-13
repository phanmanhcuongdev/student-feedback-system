package com.ttcs.backend.adapter.out.persistence.feedbackresponse;

import com.ttcs.backend.adapter.out.persistence.feedback.FeedbackEntity;
import com.ttcs.backend.adapter.out.persistence.feedback.FeedbackRepository;
import com.ttcs.backend.adapter.out.persistence.user.UserEntity;
import com.ttcs.backend.adapter.out.persistence.UserRepository;
import com.ttcs.backend.application.domain.model.FeedbackResponse;
import com.ttcs.backend.application.port.out.LoadFeedbackResponsePort;
import com.ttcs.backend.application.port.out.SaveFeedbackResponsePort;
import com.ttcs.backend.common.PersistenceAdapter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@PersistenceAdapter
@RequiredArgsConstructor
public class FeedbackResponsePersistenceAdapter implements LoadFeedbackResponsePort, SaveFeedbackResponsePort {

    private final FeedbackResponseRepository feedbackResponseRepository;
    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;

    @Override
    public List<FeedbackResponse> loadByFeedbackIds(List<Integer> feedbackIds) {
        return feedbackResponseRepository.findByFeedbackIdInOrderByCreatedAtAsc(feedbackIds).stream()
                .map(FeedbackResponseMapper::toDomain)
                .toList();
    }

    @Override
    public List<FeedbackResponse> loadByFeedbackId(Integer feedbackId) {
        return feedbackResponseRepository.findByFeedbackIdOrderByCreatedAtAsc(feedbackId).stream()
                .map(FeedbackResponseMapper::toDomain)
                .toList();
    }

    @Override
    public FeedbackResponse save(FeedbackResponse response) {
        FeedbackResponseEntity entity = new FeedbackResponseEntity();
        entity.setId(response.getId());
        entity.setContent(response.getContent());
        entity.setCreatedAt(response.getCreatedAt());

        Integer feedbackId = response.getFeedback() != null ? response.getFeedback().getId() : null;
        if (feedbackId == null) {
            throw new IllegalArgumentException("Feedback id is required when saving a feedback response");
        }

        Integer responderUserId = response.getResponder() != null ? response.getResponder().getId() : null;
        if (responderUserId == null) {
            throw new IllegalArgumentException("Responder user id is required when saving a feedback response");
        }

        FeedbackEntity feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new IllegalArgumentException("Feedback not found: " + feedbackId));
        UserEntity responder = userRepository.findById(responderUserId)
                .orElseThrow(() -> new IllegalArgumentException("Responder user not found: " + responderUserId));

        entity.setFeedback(feedback);
        entity.setResponder(responder);

        return FeedbackResponseMapper.toDomain(feedbackResponseRepository.save(entity));
    }
}
