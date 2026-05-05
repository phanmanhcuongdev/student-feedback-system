package com.ttcs.backend.adapter.out.persistence.surveyresult;

import com.ttcs.backend.adapter.out.persistence.question.QuestionEntity;
import com.ttcs.backend.adapter.out.persistence.responsedetail.ResponseDetailEntity;
import com.ttcs.backend.adapter.out.persistence.survey.SurveyEntity;
import com.ttcs.backend.application.port.out.EnterpriseSurveyReport;
import com.ttcs.backend.application.port.out.OrganizationBranding;
import com.ttcs.backend.application.port.out.QuestionStatistics;
import com.ttcs.backend.application.port.out.RatingBreakdown;
import com.ttcs.backend.application.port.out.ReportFilterCriteria;
import com.ttcs.backend.application.port.out.ReportPeriod;
import com.ttcs.backend.application.port.out.SummaryStatistics;
import com.ttcs.backend.application.port.out.SurveyReportQuestion;
import com.ttcs.backend.application.port.out.SurveyReportRatingBreakdown;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class SurveyReportDataAssembler {

    public EnterpriseSurveyReport toEnterpriseSurveyReport(
            SurveyEntity survey,
            String runtimeStatus,
            String recipientScope,
            String recipientDepartmentName,
            RecipientSummary recipientSummary,
            List<QuestionEntity> questions,
            List<ResponseDetailEntity> details,
            String generatedBy
    ) {
        List<SurveyReportQuestion> reportQuestions = toReportQuestions(questions, details);
        SummaryStatistics summaryStatistics = toSummaryStatistics(recipientSummary, reportQuestions);

        return new EnterpriseSurveyReport(
                survey.getId(),
                survey.getTitle(),
                survey.getDescription(),
                survey.getStartDate(),
                survey.getEndDate(),
                survey.getLifecycleState(),
                runtimeStatus,
                recipientScope,
                recipientDepartmentName,
                recipientSummary.targetedCount(),
                recipientSummary.openedCount(),
                recipientSummary.submittedCount(),
                recipientSummary.responseRate(),
                reportQuestions,
                generatedBy,
                LocalDateTime.now(),
                defaultBranding(),
                new ReportPeriod(survey.getStartDate(), survey.getEndDate(), "Survey active period"),
                new ReportFilterCriteria(
                        String.valueOf(survey.getId()),
                        survey.getLifecycleState(),
                        runtimeStatus,
                        recipientScope,
                        recipientDepartmentName
                ),
                summaryStatistics
        );
    }

    public List<SurveyReportQuestion> toReportQuestions(List<QuestionEntity> questions, List<ResponseDetailEntity> details) {
        Map<Integer, List<ResponseDetailEntity>> detailsByQuestionId = detailsByQuestionId(questions, details);
        return questions.stream()
                .map(question -> toSurveyReportQuestion(question, detailsByQuestionId.getOrDefault(question.getId(), List.of())))
                .toList();
    }

    public List<QuestionStatistics> toQuestionStatistics(List<QuestionEntity> questions, List<ResponseDetailEntity> details) {
        Map<Integer, List<ResponseDetailEntity>> detailsByQuestionId = detailsByQuestionId(questions, details);
        return questions.stream()
                .map(question -> toQuestionStatistics(question, detailsByQuestionId.getOrDefault(question.getId(), List.of())))
                .toList();
    }

    private Map<Integer, List<ResponseDetailEntity>> detailsByQuestionId(
            List<QuestionEntity> questions,
            List<ResponseDetailEntity> details
    ) {
        Map<Integer, List<ResponseDetailEntity>> result = new LinkedHashMap<>();
        for (QuestionEntity question : questions) {
            result.put(question.getId(), new java.util.ArrayList<>());
        }
        for (ResponseDetailEntity detail : details) {
            QuestionEntity question = detail.getQuestion();
            if (question != null) {
                result.computeIfAbsent(question.getId(), key -> new java.util.ArrayList<>()).add(detail);
            }
        }
        return result;
    }

    private SurveyReportQuestion toSurveyReportQuestion(QuestionEntity question, List<ResponseDetailEntity> details) {
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
            return new SurveyReportQuestion(
                    question.getId(),
                    question.getContent(),
                    type,
                    responseCount,
                    average,
                    counts.entrySet().stream()
                            .map(entry -> new SurveyReportRatingBreakdown(entry.getKey(), entry.getValue()))
                            .toList(),
                    List.of()
            );
        }

        List<String> comments = details.stream()
                .map(ResponseDetailEntity::getComment)
                .filter(comment -> comment != null && !comment.isBlank())
                .toList();

        return new SurveyReportQuestion(
                question.getId(),
                question.getContent(),
                type,
                responseCount,
                null,
                List.of(),
                comments
        );
    }

    private QuestionStatistics toQuestionStatistics(QuestionEntity question, List<ResponseDetailEntity> details) {
        SurveyReportQuestion reportQuestion = toSurveyReportQuestion(question, details);
        return new QuestionStatistics(
                reportQuestion.id(),
                reportQuestion.content(),
                reportQuestion.type(),
                reportQuestion.responseCount(),
                reportQuestion.averageRating(),
                reportQuestion.ratingBreakdown().stream()
                        .map(item -> new RatingBreakdown(item.rating(), item.count()))
                        .toList(),
                reportQuestion.comments()
        );
    }

    private SummaryStatistics toSummaryStatistics(RecipientSummary recipientSummary, List<SurveyReportQuestion> questions) {
        long ratingQuestions = questions.stream().filter(question -> "RATING".equalsIgnoreCase(question.type())).count();
        long commentCount = questions.stream().mapToLong(question -> question.comments().size()).sum();
        double ratingTotal = 0;
        long ratingCount = 0;

        for (SurveyReportQuestion question : questions) {
            for (SurveyReportRatingBreakdown item : question.ratingBreakdown()) {
                ratingTotal += item.rating() * item.count();
                ratingCount += item.count();
            }
        }

        return new SummaryStatistics(
                recipientSummary.targetedCount(),
                recipientSummary.openedCount(),
                recipientSummary.submittedCount(),
                recipientSummary.responseRate(),
                questions.size(),
                ratingQuestions,
                questions.size() - ratingQuestions,
                commentCount,
                ratingCount == 0 ? null : ratingTotal / ratingCount
        );
    }

    private OrganizationBranding defaultBranding() {
        return new OrganizationBranding(
                "Student Feedback System",
                null,
                "#0f766e",
                "Internal report"
        );
    }
}
