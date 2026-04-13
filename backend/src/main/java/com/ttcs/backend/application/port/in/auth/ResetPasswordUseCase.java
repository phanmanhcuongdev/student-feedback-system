package com.ttcs.backend.application.port.in.auth;

import com.ttcs.backend.application.port.in.auth.command.ResetPasswordCommand;
import com.ttcs.backend.application.port.in.auth.result.ResetPasswordResult;

public interface ResetPasswordUseCase {
    ResetPasswordResult resetPassword(ResetPasswordCommand command);
}
