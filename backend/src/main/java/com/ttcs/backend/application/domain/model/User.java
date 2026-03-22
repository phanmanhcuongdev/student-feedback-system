package com.ttcs.backend.application.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class User {
    private final Integer id;
    private final String email;
    private final String password;
    private final Role role;
    private final Boolean verified;
}