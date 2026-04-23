package com.ttcs.backend.adapter.out.persistence.question;

import com.ttcs.backend.adapter.out.persistence.survey.SurveyEntity;
import com.ttcs.backend.application.domain.model.Question;
import com.ttcs.backend.application.domain.model.QuestionType;
import com.ttcs.backend.application.domain.model.QuestionTypeMapper;

public class QuestionMapper {

    public static Question toDomain(QuestionEntity entity) {
        return new Question(
                entity.getId(),
                entity.getSurvey().getId(),
                entity.getContent(),
                entity.getContentVi(),
                entity.getContentEn(),
                entity.getSourceLang(),
                entity.isAutoTranslated(),
                entity.getModelInfo(),
                QuestionType.valueOf(entity.getType()),
                entity.getQuestionBankEntryId()
        );
    }

    public static QuestionEntity toEntity(Question domain) {
        if (domain == null) return null;

        QuestionEntity entity = new QuestionEntity();
        entity.setId(domain.getId());
        entity.setContent(domain.getContent());
        entity.setContentVi(domain.getContentVi());
        entity.setContentEn(domain.getContentEn());
        entity.setSourceLang(domain.getSourceLang());
        entity.setAutoTranslated(domain.isAutoTranslated());
        entity.setModelInfo(domain.getModelInfo());
        entity.setType(QuestionTypeMapper.toEntity(domain.getType()));
        entity.setQuestionBankEntryId(domain.getQuestionBankEntryId());

        // shallow survey
        SurveyEntity survey = new SurveyEntity();
        survey.setId(domain.getSurveyId());
        entity.setSurvey(survey);

        return entity;
    }
}
