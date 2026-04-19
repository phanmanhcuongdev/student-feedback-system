package com.ttcs.backend.adapter.out.persistence.surveyresponse;

import com.ttcs.backend.adapter.out.persistence.student.StudentMapper;
import com.ttcs.backend.adapter.out.persistence.survey.SurveyMapper;
import com.ttcs.backend.adapter.out.persistence.lecturer.LecturerMapper;
import com.ttcs.backend.application.domain.model.SurveyResponse;

public class SurveyResponseMapper {

    public static SurveyResponse toDomain(SurveyResponseEntity entity) {
        if (entity == null) return null;

        return new SurveyResponse(
                entity.getId(),
                StudentMapper.toDomain(entity.getStudent()),
                LecturerMapper.toDomain(entity.getLecturer()),
                SurveyMapper.toDomain(entity.getSurvey()),
                entity.getSubmittedAt()
        );
    }

    public static SurveyResponseEntity toEntity(SurveyResponse domain) {
        if (domain == null) return null;

        SurveyResponseEntity entity = new SurveyResponseEntity();
        entity.setId(domain.getId());
        entity.setStudent(StudentMapper.toEntity(domain.getStudent()));
        entity.setLecturer(LecturerMapper.toEntity(domain.getLecturer()));
        entity.setSurvey(SurveyMapper.toEntity(domain.getSurvey()));
        entity.setSubmittedAt(domain.getSubmittedAt());

        return entity;
    }
}