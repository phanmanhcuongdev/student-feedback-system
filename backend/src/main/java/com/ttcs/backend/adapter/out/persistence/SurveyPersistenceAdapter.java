package com.ttcs.backend.adapter.out.persistence;

import com.ttcs.backend.application.domain.model.Survey;
import com.ttcs.backend.application.port.out.LoadSurveyPort;
import com.ttcs.backend.common.PersistenceAdapter;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@PersistenceAdapter
@RequiredArgsConstructor
public class SurveyPersistenceAdapter implements LoadSurveyPort {
    private final SurveyRepository surveyRepository;

    @Override
    public Optional<Survey> loadById(Integer surveyId) {
        return surveyRepository.findById(surveyId)
                .map(SurveyMapper::toDomain);
    }
}
