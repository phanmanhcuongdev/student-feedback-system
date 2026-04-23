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
    private final String contentVi;
    private final String contentEn;
    private final String sourceLang;
    private final String modelInfo;
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
                null,
                null,
                false,
                createdAt
        );
    }

    public String translatedContentFor(String language) {
        String normalized = normalizeLanguage(language);
        return switch (normalized) {
            case "vi" -> contentVi;
            case "en" -> contentEn;
            default -> null;
        };
    }

    private String normalizeLanguage(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "";
        }
        return value.split(",")[0].trim().split("-")[0].toLowerCase();
    }
}
