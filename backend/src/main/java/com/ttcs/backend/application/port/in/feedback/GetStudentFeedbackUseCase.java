package com.ttcs.backend.application.port.in.feedback;

import com.ttcs.backend.application.port.in.feedback.result.StudentFeedbackResult;

public interface GetStudentFeedbackUseCase {
    StudentFeedbackPageResult getStudentFeedback(GetStudentFeedbackQuery query, Integer studentId);
}
