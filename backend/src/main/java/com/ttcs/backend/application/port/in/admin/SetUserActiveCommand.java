package com.ttcs.backend.application.port.in.admin;

public record SetUserActiveCommand(
        Integer targetUserId,
        Integer actorUserId,
        boolean active
) {
}
