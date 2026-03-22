package com.ttcs.backend.adapter.out.persistence;

import com.ttcs.backend.application.domain.model.SurveyResponse;

public class SurveyResponseMapper {

    public static SurveyResponse toDomain(SurveyResponseEntity entity) {
        if (entity == null) return null;

        return new SurveyResponse(
                entity.getId(),
                StudentMapper.toDomain(entity.getStudent()),
                TeacherMapper.toDomain(entity.getTeacher()),
                SurveyMapper.toDomain(entity.getSurvey()),
                entity.getSubmittedAt()
        );
    }

    public static SurveyResponseEntity toEntity(SurveyResponse domain) {
        if (domain == null) return null;

        SurveyResponseEntity entity = new SurveyResponseEntity();
        entity.setId(domain.getId());
        entity.setStudent(StudentMapper.toEntity(domain.getStudent()));
        entity.setTeacher(TeacherMapper.toEntity(domain.getTeacher()));
        entity.setSurvey(SurveyMapper.toEntity(domain.getSurvey()));
        entity.setSubmittedAt(domain.getSubmittedAt());

        return entity;
    }
}