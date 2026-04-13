package com.ttcs.backend.application.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class Feedback {
    private final Integer id;
    private final Student student;
    private final String title;
    private final String content;
    private final LocalDateTime createdAt;
}
