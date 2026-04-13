package com.ttcs.backend.application.port.in.admin;

public interface SetUserActiveUseCase {
    UserManagementActionResult setUserActive(SetUserActiveCommand command);
}
