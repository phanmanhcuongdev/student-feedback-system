package com.ttcs.backend.application.port.in.auth.command;

public record ResetPasswordCommand(String token, String newPassword) {
}
