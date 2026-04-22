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
    private final String contentOriginal;
    private final String contentTranslated;
    private final String sourceLang;
    private final boolean isAutoTranslated;
    private final LocalDateTime createdAt;

    public Feedback(Integer id, Student student, String title, String content, LocalDateTime createdAt) {
        this(
                id,
                student,
                title,
                content,
                content,
                null,
                null,
                false,
                createdAt
        );
    }
}
