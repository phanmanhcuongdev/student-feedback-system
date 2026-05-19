package com.ttcs.backend.application.domain.service;

import com.ttcs.backend.application.domain.model.ResponseDetail;
import com.ttcs.backend.application.port.in.resultview.RecordSurveyAiSummaryChangeUseCase;
import com.ttcs.backend.application.port.out.GenerateTextEmbeddingPort;
import com.ttcs.backend.application.port.out.LoadSurveyAiSummaryPort;
import com.ttcs.backend.application.port.out.SaveSurveyAiSummaryPort;
import com.ttcs.backend.common.UseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;

@UseCase
@RequiredArgsConstructor
@Slf4j
public class SurveyAiSummaryChangeService implements RecordSurveyAiSummaryChangeUseCase {

    private final SaveSurveyAiSummaryPort saveSurveyAiSummaryPort;
    private final LoadSurveyAiSummaryPort loadSurveyAiSummaryPort;
    private final GenerateTextEmbeddingPort generateTextEmbeddingPort;
    private final SurveyAiSummaryChangeScorer changeScorer;

    @Override
    public void recordSubmittedTextComments(List<ResponseDetail> responseDetails) {
        if (responseDetails == null || responseDetails.isEmpty()) {
            return;
        }

        for (ResponseDetail detail : responseDetails) {
            if (!isTrackableTextComment(detail)) {
                continue;
            }
            try {
                saveSurveyAiSummaryPort.recordTextCommentChange(toChangeCommand(detail));
            } catch (Exception exception) {
                log.warn(
                        "Skip AI summary change tracking for responseDetailId={} because tracking failed: {}",
                        detail.getId(),
                        exception.getMessage()
                );
            }
        }
    }

    private boolean isTrackableTextComment(ResponseDetail detail) {
        return detail != null
                && detail.getId() != null
                && detail.getResponse() != null
                && detail.getResponse().getSurvey() != null
                && detail.getResponse().getSurvey().getId() != null
                && detail.getQuestion() != null
                && detail.getQuestion().isText()
                && detail.getQuestion().getId() != null
                && detail.getComment() != null
                && !detail.getComment().isBlank();
    }

    private SaveSurveyAiSummaryPort.SurveyAiSummaryTextChangeCommand toChangeCommand(ResponseDetail detail) {
        int noveltyScore = noveltyScore(detail);
        SurveyAiSummaryChangeScorer.ScoredChange score = changeScorer.score(detail.getComment(), noveltyScore);

        return new SaveSurveyAiSummaryPort.SurveyAiSummaryTextChangeCommand(
                detail.getResponse().getSurvey().getId(),
                detail.getId(),
                detail.getQuestion().getId(),
                detail.getComment().strip().length(),
                score.topic(),
                score.keywordScore(),
                score.sentimentScore(),
                score.suggestionScore(),
                score.entropyImpactScore(),
                score.noveltyScore(),
                score.totalScore(),
                LocalDateTime.now()
        );
    }

    private int noveltyScore(ResponseDetail detail) {
        try {
            List<LoadSurveyAiSummaryPort.SurveyAiSummaryThemeEmbeddingRecord> themeEmbeddings =
                    loadSurveyAiSummaryPort.loadLatestThemeEmbeddings(detail.getResponse().getSurvey().getId());
            if (themeEmbeddings.isEmpty()) {
                return 0;
            }

            var embeddingResult = generateTextEmbeddingPort.embed(List.of(detail.getComment()));
            if (embeddingResult.vectors().isEmpty() || embeddingResult.vectors().getFirst().isEmpty()) {
                return 0;
            }

            double maxSimilarity = maxCosineSimilarity(embeddingResult.vectors().getFirst(), themeEmbeddings);
            if (maxSimilarity >= 0.85d) {
                return 0;
            }
            if (maxSimilarity >= 0.70d) {
                return 2;
            }
            return 4;
        } catch (Exception exception) {
            log.warn(
                    "Fallback to rule-based AI summary score for responseDetailId={} because embedding failed: {}",
                    detail.getId(),
                    exception.getMessage()
            );
            return 0;
        }
    }

    private double maxCosineSimilarity(List<Double> commentVector,
                                       List<LoadSurveyAiSummaryPort.SurveyAiSummaryThemeEmbeddingRecord> themeEmbeddings) {
        double maxSimilarity = 0.0d;
        for (LoadSurveyAiSummaryPort.SurveyAiSummaryThemeEmbeddingRecord themeEmbedding : themeEmbeddings) {
            double similarity = cosineSimilarity(commentVector, themeEmbedding.vector());
            if (similarity > maxSimilarity) {
                maxSimilarity = similarity;
            }
        }
        return maxSimilarity;
    }

    private double cosineSimilarity(List<Double> left, List<Double> right) {
        if (left == null || right == null || left.isEmpty() || right.isEmpty() || left.size() != right.size()) {
            return 0.0d;
        }

        double dot = 0.0d;
        double leftNorm = 0.0d;
        double rightNorm = 0.0d;
        for (int index = 0; index < left.size(); index++) {
            double leftValue = value(left.get(index));
            double rightValue = value(right.get(index));
            dot += leftValue * rightValue;
            leftNorm += leftValue * leftValue;
            rightNorm += rightValue * rightValue;
        }
        if (leftNorm == 0.0d || rightNorm == 0.0d) {
            return 0.0d;
        }
        return dot / (Math.sqrt(leftNorm) * Math.sqrt(rightNorm));
    }

    private double value(Double value) {
        return value == null ? 0.0d : value;
    }
}
