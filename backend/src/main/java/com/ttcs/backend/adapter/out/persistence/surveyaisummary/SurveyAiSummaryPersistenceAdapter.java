package com.ttcs.backend.adapter.out.persistence.surveyaisummary;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ttcs.backend.adapter.out.persistence.survey.SurveyRepository;
import com.ttcs.backend.application.domain.model.SurveyAiSummaryJobStatus;
import com.ttcs.backend.application.port.out.LoadSurveyAiSummaryPort;
import com.ttcs.backend.application.port.out.SaveSurveyAiSummaryPort;
import com.ttcs.backend.common.PersistenceAdapter;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@PersistenceAdapter
@RequiredArgsConstructor
public class SurveyAiSummaryPersistenceAdapter implements LoadSurveyAiSummaryPort, SaveSurveyAiSummaryPort {

    private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<>() {
    };

    private final SurveyAiSummaryRepository surveyAiSummaryRepository;
    private final SurveyAiSummaryJobRepository surveyAiSummaryJobRepository;
    private final SurveyRepository surveyRepository;
    private final EntityManager entityManager;
    private final ObjectMapper objectMapper;

    @Override
    public Optional<SurveyAiSummaryRecord> loadLatestSummary(Integer surveyId) {
        return surveyAiSummaryRepository.findFirstBySurvey_IdOrderByCreatedAtDesc(surveyId)
                .map(this::toSummaryRecord);
    }

    @Override
    public Optional<SurveyAiSummaryJobRecord> loadLatestJob(Integer surveyId) {
        return surveyAiSummaryJobRepository.findFirstBySurvey_IdOrderByCreatedAtDesc(surveyId)
                .map(this::toJobRecord);
    }

    @Override
    public Optional<SurveyAiSummaryRecord> loadSummaryById(Integer summaryId) {
        return surveyAiSummaryRepository.findById(summaryId)
                .map(this::toSummaryRecord);
    }

    @Override
    public SurveyAiSummaryPayload loadSurveySummaryPayload(Integer surveyId) {
        String surveyTitle = surveyRepository.findById(surveyId)
                .map(item -> item.getTitle())
                .orElse(null);

        Query query = entityManager.createNativeQuery("""
                SELECT
                    q.question_id,
                    q.content,
                    rd.comment
                FROM Response_Detail rd
                INNER JOIN Question q ON q.question_id = rd.question_id
                INNER JOIN Survey_Response sr ON sr.response_id = rd.response_id
                WHERE sr.survey_id = :surveyId
                    AND q.type = 'TEXT'
                    AND rd.comment IS NOT NULL
                    AND LTRIM(RTRIM(rd.comment)) <> ''
                ORDER BY q.question_id ASC, rd.id ASC
                """);
        query.setParameter("surveyId", surveyId);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        List<QuestionCommentPayload> comments = rows.stream()
                .map(row -> new QuestionCommentPayload(
                        ((Number) row[0]).intValue(),
                        row[1] != null ? row[1].toString() : "",
                        row[2] != null ? row[2].toString() : ""
                ))
                .toList();

        return new SurveyAiSummaryPayload(
                surveyId,
                surveyTitle,
                comments.size(),
                comments
        );
    }

    @Override
    @Transactional
    public SurveyAiSummaryJobRecord createJob(Integer surveyId, String sourceHash, Integer commentCount, Integer requestedByUserId) {
        SurveyAiSummaryJobEntity entity = new SurveyAiSummaryJobEntity();
        entity.setSurvey(surveyRepository.getReferenceById(surveyId));
        entity.setSourceHash(sourceHash);
        entity.setCommentCount(commentCount);
        entity.setStatus(SurveyAiSummaryJobStatus.QUEUED.name());
        entity.setRequestedByUserId(requestedByUserId);
        entity.setCreatedAt(LocalDateTime.now());
        return toJobRecord(surveyAiSummaryJobRepository.save(entity));
    }

    @Override
    @Transactional
    public void markJobProcessing(Integer jobId, LocalDateTime startedAt) {
        SurveyAiSummaryJobEntity entity = surveyAiSummaryJobRepository.getReferenceById(jobId);
        entity.setStatus(SurveyAiSummaryJobStatus.PROCESSING.name());
        entity.setStartedAt(startedAt);
    }

    @Override
    @Transactional
    public void markJobCompleted(Integer jobId, Integer summaryId, LocalDateTime finishedAt) {
        SurveyAiSummaryJobEntity entity = surveyAiSummaryJobRepository.getReferenceById(jobId);
        entity.setStatus(SurveyAiSummaryJobStatus.COMPLETED.name());
        entity.setSummary(summaryId == null ? null : surveyAiSummaryRepository.getReferenceById(summaryId));
        entity.setFinishedAt(finishedAt);
        entity.setErrorMessage(null);
    }

    @Override
    @Transactional
    public void markJobFailed(Integer jobId, String errorMessage, LocalDateTime finishedAt) {
        SurveyAiSummaryJobEntity entity = surveyAiSummaryJobRepository.getReferenceById(jobId);
        entity.setStatus(SurveyAiSummaryJobStatus.FAILED.name());
        entity.setFinishedAt(finishedAt);
        entity.setErrorMessage(errorMessage);
    }

    @Override
    @Transactional
    public SurveyAiSummaryRecord saveSummary(Integer surveyId, String sourceHash, String modelName, Integer commentCount,
                                             String summaryText, List<String> highlights, List<String> concerns,
                                             List<String> actions, Integer createdByUserId) {
        SurveyAiSummaryEntity entity = new SurveyAiSummaryEntity();
        entity.setSurvey(surveyRepository.getReferenceById(surveyId));
        entity.setSourceHash(sourceHash);
        entity.setModelName(modelName);
        entity.setCommentCount(commentCount);
        entity.setSummaryText(summaryText);
        entity.setHighlightsJson(writeJson(highlights));
        entity.setConcernsJson(writeJson(concerns));
        entity.setActionsJson(writeJson(actions));
        entity.setCreatedByUserId(createdByUserId);
        entity.setCreatedAt(LocalDateTime.now());
        return toSummaryRecord(surveyAiSummaryRepository.save(entity));
    }

    private SurveyAiSummaryRecord toSummaryRecord(SurveyAiSummaryEntity entity) {
        return new SurveyAiSummaryRecord(
                entity.getId(),
                entity.getSurvey().getId(),
                entity.getSourceHash(),
                entity.getModelName(),
                entity.getCommentCount(),
                entity.getSummaryText(),
                readJson(entity.getHighlightsJson()),
                readJson(entity.getConcernsJson()),
                readJson(entity.getActionsJson()),
                entity.getCreatedByUserId(),
                entity.getCreatedAt()
        );
    }

    private SurveyAiSummaryJobRecord toJobRecord(SurveyAiSummaryJobEntity entity) {
        return new SurveyAiSummaryJobRecord(
                entity.getId(),
                entity.getSurvey().getId(),
                entity.getSourceHash(),
                entity.getCommentCount(),
                SurveyAiSummaryJobStatus.valueOf(entity.getStatus()),
                entity.getRequestedByUserId(),
                entity.getSummary() != null ? entity.getSummary().getId() : null,
                entity.getCreatedAt(),
                entity.getStartedAt(),
                entity.getFinishedAt(),
                entity.getErrorMessage()
        );
    }

    private String writeJson(List<String> values) {
        try {
            return objectMapper.writeValueAsString(values == null ? List.of() : values);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to serialize AI summary payload", exception);
        }
    }

    private List<String> readJson(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(value, STRING_LIST_TYPE);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to parse AI summary payload", exception);
        }
    }
}
