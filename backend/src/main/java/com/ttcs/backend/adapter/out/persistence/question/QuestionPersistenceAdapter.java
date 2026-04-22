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
        saveAllReturning(questions);
    }

    @Override
    public List<Question> saveAllReturning(List<Question> questions) {
        List<QuestionEntity> entities = questions.stream()
                .map(QuestionMapper::toEntity)
                .toList();
        return questionRepository.saveAll(entities).stream()
                .map(QuestionMapper::toDomain)
                .toList();
    }

    @Override
    public void replaceSurveyQuestions(Integer surveyId, List<Question> questions) {
        replaceSurveyQuestionsReturning(surveyId, questions);
    }

    @Override
    public List<Question> replaceSurveyQuestionsReturning(Integer surveyId, List<Question> questions) {
        questionRepository.deleteBySurvey_Id(surveyId);
        return saveAllReturning(questions);
    }
}
