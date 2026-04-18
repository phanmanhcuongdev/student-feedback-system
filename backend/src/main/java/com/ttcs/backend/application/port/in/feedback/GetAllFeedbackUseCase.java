package com.ttcs.backend.application.port.in.feedback;

public interface GetAllFeedbackUseCase {
    StaffFeedbackPageResult getAllFeedback(GetAllFeedbackQuery query);
}
