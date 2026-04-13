package com.ttcs.backend.application.port.in.auth;

import com.ttcs.backend.application.port.in.auth.command.ForgotPasswordCommand;
import com.ttcs.backend.application.port.in.auth.result.ForgotPasswordResult;

public interface ForgotPasswordUseCase {
    ForgotPasswordResult forgotPassword(ForgotPasswordCommand command);
}
