package com.ttcs.backend.adapter.out.persistence.feedback;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedbackRepository extends JpaRepository<FeedbackEntity, Integer> {
    List<FeedbackEntity> findByStudentIdOrderByCreatedAtDesc(Integer studentId);

    List<FeedbackEntity> findAllByOrderByCreatedAtDesc();
}
