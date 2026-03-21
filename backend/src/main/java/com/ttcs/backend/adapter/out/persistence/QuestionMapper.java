package com.ttcs.backend.adapter.out.persistence;

import com.ttcs.backend.application.domain.model.Question;
import com.ttcs.backend.application.domain.model.QuestionType;

public class QuestionMapper {

    public static Question toDomain(QuestionEntity entity) {
        return new Question(
                entity.getId(),
                entity.getSurvey().getId(),
                entity.getContent(),
                QuestionType.valueOf(entity.getType())
        );
    }
}