package com.ttcs.backend.application.port.in.auth.command;

public record LoginCommand(
        String email,
        String password
) {
}
