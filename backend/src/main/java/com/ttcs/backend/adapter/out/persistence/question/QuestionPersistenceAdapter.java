package com.ttcs.backend.adapter.out.persistence.question;

import com.ttcs.backend.application.domain.model.Question;
import com.ttcs.backend.application.port.out.LoadQuestionPort;
import com.ttcs.backend.common.PersistenceAdapter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@PersistenceAdapter
@RequiredArgsConstructor
public class QuestionPersistenceAdapter implements LoadQuestionPort, com.ttcs.backend.application.port.out.SaveQuestionPort {

    private final QuestionRepository questionRepository;

    @Override
    public List<Question> loadBySurveyId(Integer surveyId) {
        return questionRepository.findBySurvey_IdOrderByIdAsc(surveyId)
                .stream()
                .map(QuestionMapper::toDomain)
                .toList();
    }

    @Override
    public void saveAll(List<Question> questions) {
        List<QuestionEntity> entities = questions.stream()
                .map(QuestionMapper::toEntity)
                .toList();
        questionRepository.saveAll(entities);
    }
}
