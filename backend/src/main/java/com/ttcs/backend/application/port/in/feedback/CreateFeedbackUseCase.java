package com.ttcs.backend.application.port.in.feedback;

import com.ttcs.backend.application.port.in.feedback.command.CreateFeedbackCommand;
import com.ttcs.backend.application.port.in.feedback.result.CreateFeedbackResult;

public interface CreateFeedbackUseCase {
    CreateFeedbackResult createFeedback(CreateFeedbackCommand command);
}
