package com.ttcs.backend.adapter.out.persistence.surveyresult;

import com.ttcs.backend.adapter.out.persistence.question.QuestionEntity;
import com.ttcs.backend.adapter.out.persistence.responsedetail.ResponseDetailEntity;
import com.ttcs.backend.adapter.out.persistence.responsedetail.ResponseDetailRepository;
import com.ttcs.backend.adapter.out.persistence.survey.SurveyEntity;
import com.ttcs.backend.adapter.out.persistence.survey.SurveyMapper;
import com.ttcs.backend.adapter.out.persistence.survey.SurveyRepository;
import com.ttcs.backend.adapter.out.persistence.surveyresponse.SurveyResponseRepository;
import com.ttcs.backend.application.port.in.resultview.QuestionStatisticsResult;
import com.ttcs.backend.application.port.in.resultview.RatingBreakdownResult;
import com.ttcs.backend.application.port.in.resultview.SurveyResultDetailResult;
import com.ttcs.backend.application.port.in.resultview.SurveyResultSummaryResult;
import com.ttcs.backend.application.port.out.LoadSurveyResultPort;
import com.ttcs.backend.common.PersistenceAdapter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@PersistenceAdapter
@RequiredArgsConstructor
public class SurveyResultPersistenceAdapter implements LoadSurveyResultPort {

    private final SurveyRepository surveyRepository;
    private final SurveyResponseRepository surveyResponseRepository;
    private final ResponseDetailRepository responseDetailRepository;

    @Override
    public List<SurveyResultSummaryResult> loadSurveyResults() {
        return surveyRepository.findAll().stream()
                .sorted(Comparator.comparing(SurveyEntity::getId))
                .map(survey -> new SurveyResultSummaryResult(
                        survey.getId(),
                        survey.getTitle(),
                        survey.getDescription(),
                        survey.getStartDate(),
                        survey.getEndDate(),
                        SurveyMapper.toDomain(survey).status().name(),
                        surveyResponseRepository.countBySurvey_Id(survey.getId())
                ))
                .toList();
    }

    @Override
    public Optional<SurveyResultDetailResult> loadSurveyResult(Integer surveyId) {
        SurveyEntity survey = surveyRepository.findById(surveyId).orElse(null);
        if (survey == null) {
            return Optional.empty();
        }

        List<QuestionEntity> questions = responseDetailRepository.findQuestionsBySurveyId(surveyId);
        List<ResponseDetailEntity> details = responseDetailRepository.findAllBySurveyIdForResults(surveyId);
        long responseCount = surveyResponseRepository.countBySurvey_Id(surveyId);

        Map<Integer, List<ResponseDetailEntity>> detailsByQuestionId = new LinkedHashMap<>();
        for (QuestionEntity question : questions) {
            detailsByQuestionId.put(question.getId(), new ArrayList<>());
        }
        for (ResponseDetailEntity detail : details) {
            QuestionEntity question = detail.getQuestion();
            if (question != null) {
                detailsByQuestionId.computeIfAbsent(question.getId(), key -> new ArrayList<>()).add(detail);
            }
        }

        List<QuestionStatisticsResult> questionResults = questions.stream()
                .map(question -> toQuestionStatistics(question, detailsByQuestionId.getOrDefault(question.getId(), List.of())))
                .toList();

        return Optional.of(new SurveyResultDetailResult(
                survey.getId(),
                survey.getTitle(),
                survey.getDescription(),
                survey.getStartDate(),
                survey.getEndDate(),
                SurveyMapper.toDomain(survey).status().name(),
                responseCount,
                questionResults
        ));
    }

    private QuestionStatisticsResult toQuestionStatistics(QuestionEntity question, List<ResponseDetailEntity> details) {
        String type = question.getType();
        long responseCount = details.size();

        if ("RATING".equalsIgnoreCase(type)) {
            Map<Integer, Long> counts = new LinkedHashMap<>();
            for (int rating = 1; rating <= 5; rating++) {
                counts.put(rating, 0L);
            }

            double total = 0;
            long ratedCount = 0;
            for (ResponseDetailEntity detail : details) {
                Integer rating = detail.getRating();
                if (rating != null) {
                    counts.put(rating, counts.getOrDefault(rating, 0L) + 1);
                    total += rating;
                    ratedCount++;
                }
            }

            Double average = ratedCount == 0 ? null : total / ratedCount;

            return new QuestionStatisticsResult(
                    question.getId(),
                    question.getContent(),
                    type,
                    responseCount,
                    average,
                    counts.entrySet().stream()
                            .map(entry -> new RatingBreakdownResult(entry.getKey(), entry.getValue()))
                            .toList(),
                    List.of()
            );
        }

        List<String> comments = details.stream()
                .map(ResponseDetailEntity::getComment)
                .filter(comment -> comment != null && !comment.isBlank())
                .toList();

        return new QuestionStatisticsResult(
                question.getId(),
                question.getContent(),
                type,
                responseCount,
                null,
                List.of(),
                comments
        );
    }
}
