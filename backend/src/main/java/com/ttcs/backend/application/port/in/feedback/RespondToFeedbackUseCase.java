package com.ttcs.backend.application.port.in.feedback;

import com.ttcs.backend.application.port.in.feedback.command.RespondToFeedbackCommand;
import com.ttcs.backend.application.port.in.feedback.result.RespondToFeedbackResult;

public interface RespondToFeedbackUseCase {
    RespondToFeedbackResult respond(RespondToFeedbackCommand command);
}
