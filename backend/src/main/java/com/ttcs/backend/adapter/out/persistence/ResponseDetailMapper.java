package com.ttcs.backend.adapter.out.persistence;

import com.ttcs.backend.application.domain.model.ResponseDetail;

public class ResponseDetailMapper {

    public static ResponseDetail toDomain(ResponseDetailEntity entity) {
        if (entity == null) return null;

        return new ResponseDetail(
                entity.getId(),
                SurveyResponseMapper.toDomain(entity.getResponse()),
                QuestionMapper.toDomain(entity.getQuestion()),
                entity.getRating(),
                entity.getComment()
        );
    }

    public static ResponseDetailEntity toEntity(ResponseDetail domain) {
        if (domain == null) return null;

        ResponseDetailEntity entity = new ResponseDetailEntity();
        entity.setId(domain.getId());
        entity.setResponse(SurveyResponseMapper.toEntity(domain.getResponse()));
        entity.setQuestion(QuestionMapper.toEntity(domain.getQuestion()));
        entity.setRating(domain.getRating());
        entity.setComment(domain.getComment());

        return entity;
    }
}