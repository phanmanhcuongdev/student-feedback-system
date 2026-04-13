package com.ttcs.backend.application.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ManagedUser {
    private final User user;
    private final String name;
    private final Department department;
    private final String studentCode;
    private final String teacherCode;
    private final Status studentStatus;
}
