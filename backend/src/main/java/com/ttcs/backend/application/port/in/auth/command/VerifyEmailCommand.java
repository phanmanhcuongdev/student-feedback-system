package com.ttcs.backend.application.port.in.auth.command;

public record VerifyEmailCommand(
        String token
) {
}
