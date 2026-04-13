package com.ttcs.backend.application.port.in.feedback;

import com.ttcs.backend.application.port.in.feedback.result.StaffFeedbackResult;

import java.util.List;

public interface GetAllFeedbackUseCase {
    List<StaffFeedbackResult> getAllFeedback();
}
