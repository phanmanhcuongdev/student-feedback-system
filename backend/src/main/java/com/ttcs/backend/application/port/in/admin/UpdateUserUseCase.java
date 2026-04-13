package com.ttcs.backend.application.port.in.admin;

public interface UpdateUserUseCase {
    UserManagementActionResult updateUser(UpdateUserCommand command);
}
