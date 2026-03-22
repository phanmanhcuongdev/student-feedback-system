package com.ttcs.backend.adapter.out.persistence;


import com.ttcs.backend.application.domain.model.SurveyResponse;
import com.ttcs.backend.application.port.out.LoadSurveyResponsePort;
import com.ttcs.backend.application.port.out.SaveSurveyResponsePort;
import com.ttcs.backend.common.PersistenceAdapter;
import lombok.RequiredArgsConstructor;


@PersistenceAdapter
@RequiredArgsConstructor
public class SurveyResponsePersistenceAdapter implements LoadSurveyResponsePort, SaveSurveyResponsePort {

    private final SurveyResponseRepository surveyResponseRepository;

    @Override
    public boolean existsBySurveyIdAndStudentId(Integer surveyId, Integer studentId) {
        return surveyResponseRepository.existsBySurveyIdAndStudentId(surveyId, studentId);
    }

    @Override
    public SurveyResponse save(SurveyResponse surveyResponse) {
        SurveyResponseEntity savedEntity =
                surveyResponseRepository.save(SurveyResponseMapper.toEntity(surveyResponse));
        return SurveyResponseMapper.toDomain(savedEntity);
    }
}