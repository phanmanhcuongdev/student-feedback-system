package com.ttcs.backend.application.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Admin {
    private final Integer id;
    private final User user;
    private final String name;
}