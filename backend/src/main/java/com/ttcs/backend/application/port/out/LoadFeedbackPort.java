package com.ttcs.backend.application.port.out;

import com.ttcs.backend.application.domain.model.Feedback;

import java.util.List;
import java.util.Optional;

public interface LoadFeedbackPort {
    List<Feedback> loadByStudentId(Integer studentId);
    StudentFeedbackSearchPage loadStudentPage(LoadStudentFeedbackQuery query);

    StaffFeedbackSearchPage loadPage(LoadFeedbackQuery query);

    Optional<Feedback> loadById(Integer feedbackId);
}
