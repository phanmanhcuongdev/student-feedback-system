package com.ttcs.backend.application.port.in.auth;

import com.ttcs.backend.application.port.in.auth.command.VerifyEmailCommand;
import com.ttcs.backend.application.port.in.auth.result.VerifyEmailResult;

public interface VerifyEmailUseCase {
    VerifyEmailResult verify(VerifyEmailCommand command);
}
