package com.ttcs.backend.application.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Student {
    private final Integer id;
    private final User user;
    private final String name;
    private final String studentCode;
    private final Department department;
    private final Status status;
    private final String studentCardImageUrl;
    private final String nationalIdImageUrl;
}