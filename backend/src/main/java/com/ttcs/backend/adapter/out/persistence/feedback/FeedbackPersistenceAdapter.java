package com.ttcs.backend.adapter.out.persistence.feedback;

import com.ttcs.backend.adapter.out.persistence.student.StudentEntity;
import com.ttcs.backend.adapter.out.persistence.student.StudentRepository;
import com.ttcs.backend.application.domain.model.Feedback;
import com.ttcs.backend.application.port.out.LoadFeedbackPort;
import com.ttcs.backend.application.port.out.SaveFeedbackPort;
import com.ttcs.backend.common.PersistenceAdapter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@PersistenceAdapter
@RequiredArgsConstructor
public class FeedbackPersistenceAdapter implements LoadFeedbackPort, SaveFeedbackPort {

    private final FeedbackRepository feedbackRepository;
    private final StudentRepository studentRepository;

    @Override
    public List<Feedback> loadByStudentId(Integer studentId) {
        return feedbackRepository.findByStudentIdOrderByCreatedAtDesc(studentId).stream()
                .map(FeedbackMapper::toDomain)
                .toList();
    }

    @Override
    public List<Feedback> loadAll() {
        return feedbackRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(FeedbackMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Feedback> loadById(Integer feedbackId) {
        return feedbackRepository.findById(feedbackId).map(FeedbackMapper::toDomain);
    }

    @Override
    public Feedback save(Feedback feedback) {
        FeedbackEntity entity = new FeedbackEntity();
        entity.setId(feedback.getId());
        entity.setTitle(feedback.getTitle());
        entity.setContent(feedback.getContent());
        entity.setCreatedAt(feedback.getCreatedAt());

        Integer studentId = feedback.getStudent() != null ? feedback.getStudent().getId() : null;
        if (studentId == null) {
            throw new IllegalArgumentException("Student id is required when saving feedback");
        }

        StudentEntity student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));
        entity.setStudent(student);

        return FeedbackMapper.toDomain(feedbackRepository.save(entity));
    }
}
