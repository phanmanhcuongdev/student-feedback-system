package com.ttcs.backend.application.port.in.auth;

import com.ttcs.backend.application.port.in.auth.command.ChangePasswordCommand;
import com.ttcs.backend.application.port.in.auth.result.ChangePasswordResult;

public interface ChangePasswordUseCase {
    ChangePasswordResult changePassword(ChangePasswordCommand command);
}
