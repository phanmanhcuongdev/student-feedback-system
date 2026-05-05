package com.ttcs.backend.infrastructure.configuration;

import java.security.Principal;

public record StompUserPrincipal(Integer userId) implements Principal {

    @Override
    public String getName() {
        return String.valueOf(userId);
    }
}
