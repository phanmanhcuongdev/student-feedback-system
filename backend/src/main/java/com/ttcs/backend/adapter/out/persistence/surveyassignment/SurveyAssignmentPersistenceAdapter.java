package com.ttcs.backend.adapter.out.persistence.surveyassignment;

import com.ttcs.backend.adapter.out.persistence.survey.SurveyEntity;
import com.ttcs.backend.adapter.out.persistence.survey.SurveyRepository;
import com.ttcs.backend.application.domain.model.SurveyAssignment;
import com.ttcs.backend.application.port.out.LoadSurveyAssignmentPort;
import com.ttcs.backend.application.port.out.SaveSurveyAssignmentPort;
import com.ttcs.backend.common.PersistenceAdapter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@PersistenceAdapter
@RequiredArgsConstructor
public class SurveyAssignmentPersistenceAdapter implements LoadSurveyAssignmentPort, SaveSurveyAssignmentPort {

    private final SurveyAssignmentRepository surveyAssignmentRepository;
    private final SurveyRepository surveyRepository;

    @Override
    public List<SurveyAssignment> loadBySurveyId(Integer surveyId) {
        return surveyAssignmentRepository.findBySurveyIdOrderByIdAsc(surveyId).stream()
                .map(SurveyAssignmentMapper::toDomain)
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    @Override
    public void replaceAssignments(Integer surveyId, List<SurveyAssignment> assignments) {
        surveyAssignmentRepository.deleteBySurveyId(surveyId);
        SurveyEntity survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new IllegalArgumentException("Survey not found: " + surveyId));

        List<SurveyAssignmentEntity> entities = assignments.stream()
                .map(item -> {
                    SurveyAssignmentEntity entity = new SurveyAssignmentEntity();
                    entity.setSurvey(survey);
                    entity.setEvaluatorType(item.getEvaluatorType().name());
                    entity.setEvaluatorValue(item.getEvaluatorValue());
                    entity.setSubjectType(item.getSubjectType().name());
                    entity.setSubjectValue(item.getSubjectValue());
                    return entity;
                })
                .toList();
        surveyAssignmentRepository.saveAll(entities);
    }
}
