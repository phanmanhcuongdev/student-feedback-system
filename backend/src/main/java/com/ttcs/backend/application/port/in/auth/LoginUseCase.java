package com.ttcs.backend.application.port.in.auth;

import com.ttcs.backend.application.port.in.auth.command.LoginCommand;
import com.ttcs.backend.application.port.in.auth.result.LoginResult;

public interface LoginUseCase {
    LoginResult login(LoginCommand command);
}
