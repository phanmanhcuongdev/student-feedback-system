package com.ttcs.backend.application.port.in.feedback;

import com.ttcs.backend.application.port.in.feedback.result.StudentFeedbackResult;

import java.util.List;

public interface GetStudentFeedbackUseCase {
    List<StudentFeedbackResult> getStudentFeedback(Integer studentId);
}
