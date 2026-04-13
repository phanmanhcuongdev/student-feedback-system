package com.ttcs.backend.adapter.out.persistence.feedbackresponse;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedbackResponseRepository extends JpaRepository<FeedbackResponseEntity, Integer> {
    List<FeedbackResponseEntity> findByFeedbackIdInOrderByCreatedAtAsc(List<Integer> feedbackIds);

    List<FeedbackResponseEntity> findByFeedbackIdOrderByCreatedAtAsc(Integer feedbackId);
}
