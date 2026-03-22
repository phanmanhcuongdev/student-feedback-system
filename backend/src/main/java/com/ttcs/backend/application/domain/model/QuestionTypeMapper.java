package com.ttcs.backend.application.domain.model;

public class QuestionTypeMapper {

    public static QuestionType toDomain(String type) {
        if (type == null) return null;
        return QuestionType.valueOf(type);
    }

    public static String toEntity(QuestionType type) {
        if (type == null) return null;
        return type.name();
    }
}