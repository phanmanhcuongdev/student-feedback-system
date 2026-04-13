package com.ttcs.backend.application.port.in.auth.command;

public record ChangePasswordCommand(
        Integer userId,
        String currentPassword,
        String newPassword
) {
}
