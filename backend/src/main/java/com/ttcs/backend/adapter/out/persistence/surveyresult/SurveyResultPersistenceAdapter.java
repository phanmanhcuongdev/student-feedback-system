package com.ttcs.backend.adapter.out.persistence.surveyresult;

import com.ttcs.backend.adapter.out.persistence.DepartmentRepository;
import com.ttcs.backend.adapter.out.persistence.question.QuestionEntity;
import com.ttcs.backend.adapter.out.persistence.responsedetail.ResponseDetailEntity;
import com.ttcs.backend.adapter.out.persistence.responsedetail.ResponseDetailRepository;
import com.ttcs.backend.adapter.out.persistence.surveyassignment.SurveyAssignmentEntity;
import com.ttcs.backend.adapter.out.persistence.surveyassignment.SurveyAssignmentRepository;
import com.ttcs.backend.adapter.out.persistence.surveyrecipient.SurveyRecipientRepository;
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
    private final SurveyRecipientRepository surveyRecipientRepository;
    private final SurveyAssignmentRepository surveyAssignmentRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    public List<SurveyResultSummaryResult> loadSurveyResults() {
        return surveyRepository.findAll().stream()
                .sorted(Comparator.comparing(SurveyEntity::getId))
                .map(survey -> {
                    RecipientSummary recipientSummary = recipientSummary(survey.getId());
                    return new SurveyResultSummaryResult(
                            survey.getId(),
                            survey.getTitle(),
                            survey.getDescription(),
                            survey.getStartDate(),
                            survey.getEndDate(),
                            SurveyMapper.toDomain(survey).status().name(),
                            survey.getLifecycleState(),
                            SurveyMapper.toDomain(survey).status().name(),
                            recipientScope(survey.getId()),
                            recipientDepartmentName(survey.getId()),
                            surveyResponseRepository.countBySurvey_Id(survey.getId()),
                            recipientSummary.targetedCount(),
                            recipientSummary.openedCount(),
                            recipientSummary.submittedCount(),
                            recipientSummary.responseRate()
                    );
                })
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
        RecipientSummary recipientSummary = recipientSummary(surveyId);

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
                survey.getLifecycleState(),
                SurveyMapper.toDomain(survey).status().name(),
                recipientScope(surveyId),
                recipientDepartmentName(surveyId),
                responseCount,
                recipientSummary.targetedCount(),
                recipientSummary.openedCount(),
                recipientSummary.submittedCount(),
                recipientSummary.responseRate(),
                questionResults
        ));
    }

    private RecipientSummary recipientSummary(Integer surveyId) {
        List<com.ttcs.backend.adapter.out.persistence.surveyrecipient.SurveyRecipientEntity> recipients =
                surveyRecipientRepository.findBySurvey_IdOrderByIdAsc(surveyId);
        long targetedCount = recipients.size();
        long openedCount = recipients.stream().filter(item -> item.getOpenedAt() != null).count();
        long submittedCount = recipients.stream().filter(item -> item.getSubmittedAt() != null).count();
        double responseRate = targetedCount == 0 ? 0.0 : (submittedCount * 100.0) / targetedCount;
        return new RecipientSummary(targetedCount, openedCount, submittedCount, responseRate);
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

    private String recipientScope(Integer surveyId) {
        SurveyAssignmentEntity assignment = firstAssignment(surveyId);
        if (assignment == null) {
            return "ALL_STUDENTS";
        }
        return "DEPARTMENT".equalsIgnoreCase(assignment.getSubjectType()) ? "DEPARTMENT" : "ALL_STUDENTS";
    }

    private String recipientDepartmentName(Integer surveyId) {
        SurveyAssignmentEntity assignment = firstAssignment(surveyId);
        if (assignment == null) {
            return null;
        }
        if (!"DEPARTMENT".equalsIgnoreCase(assignment.getSubjectType())) {
            return null;
        }
        if (assignment.getSubjectValue() == null) {
            return null;
        }
        return departmentRepository.findById(assignment.getSubjectValue())
                .map(item -> item.getName())
                .orElse(null);
    }

    private SurveyAssignmentEntity firstAssignment(Integer surveyId) {
        return surveyAssignmentRepository.findBySurveyIdOrderByIdAsc(surveyId).stream().findFirst().orElse(null);
    }

    private record RecipientSummary(long targetedCount, long openedCount, long submittedCount, double responseRate) {
    }
}
