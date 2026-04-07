package com.ttcs.backend.application.port.in.auth;

import com.ttcs.backend.application.port.in.auth.command.RegisterStudentCommand;
import com.ttcs.backend.application.port.in.auth.result.RegisterStudentResult;

public interface RegisterStudentUseCase {
    RegisterStudentResult register(RegisterStudentCommand command);
}
