package com.ttcs.backend.adapter.out.persistence.surveyaisummary;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ttcs.backend.adapter.out.persistence.question.QuestionEntity;
import com.ttcs.backend.adapter.out.persistence.responsedetail.ResponseDetailEntity;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@PersistenceAdapter
@RequiredArgsConstructor
public class SurveyAiSummaryPersistenceAdapter implements LoadSurveyAiSummaryPort, SaveSurveyAiSummaryPort {

    private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<>() {
    };
    private static final TypeReference<Map<String, Integer>> TOPIC_COUNT_TYPE = new TypeReference<>() {
    };

    private final SurveyAiSummaryRepository surveyAiSummaryRepository;
    private final SurveyAiSummaryJobRepository surveyAiSummaryJobRepository;
    private final SurveyAiSourceStateRepository surveyAiSourceStateRepository;
    private final SurveyAiPendingChangeRepository surveyAiPendingChangeRepository;
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
    public Optional<SurveyAiSummaryJobRecord> loadActiveJob(Integer surveyId) {
        Query query = entityManager.createNativeQuery("""
                SELECT TOP 1 *
                FROM Survey_AI_Summary_Job
                WHERE survey_id = ?1
                    AND status IN ('QUEUED', 'PROCESSING')
                ORDER BY created_at ASC, job_id ASC
                """, SurveyAiSummaryJobEntity.class);
        query.setParameter(1, surveyId);
        @SuppressWarnings("unchecked")
        List<SurveyAiSummaryJobEntity> rows = query.getResultList();
        return rows.stream().findFirst().map(this::toJobRecord);
    }

    @Override
    public Optional<SurveyAiSummaryRecord> loadSummaryById(Integer summaryId) {
        return surveyAiSummaryRepository.findById(summaryId)
                .map(this::toSummaryRecord);
    }

    @Override
    public Optional<SurveyAiSummarySourceStateRecord> loadSourceState(Integer surveyId) {
        return surveyAiSourceStateRepository.findById(surveyId)
                .map(this::toSourceStateRecord);
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
                WHERE sr.survey_id = ?1
                    AND q.type = 'TEXT'
                    AND rd.comment IS NOT NULL
                    AND LTRIM(RTRIM(rd.comment)) <> ''
                ORDER BY q.question_id ASC, rd.id ASC
                """);
        query.setParameter(1, surveyId);

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
    public boolean markJobProcessingIfNoActiveJob(Integer jobId, Integer surveyId, LocalDateTime startedAt) {
        Query query = entityManager.createNativeQuery("""
                UPDATE Survey_AI_Summary_Job
                SET status = 'PROCESSING',
                    started_at = ?3,
                    error_message = NULL
                WHERE job_id = ?1
                    AND survey_id = ?2
                    AND status = 'QUEUED'
                    AND NOT EXISTS (
                        SELECT 1
                        FROM Survey_AI_Summary_Job active WITH (UPDLOCK, HOLDLOCK)
                        WHERE active.survey_id = ?2
                            AND active.status = 'PROCESSING'
                    )
                    AND job_id = (
                        SELECT TOP 1 queued.job_id
                        FROM Survey_AI_Summary_Job queued WITH (UPDLOCK, HOLDLOCK)
                        WHERE queued.survey_id = ?2
                            AND queued.status = 'QUEUED'
                        ORDER BY queued.created_at ASC, queued.job_id ASC
                    )
                """);
        query.setParameter(1, jobId);
        query.setParameter(2, surveyId);
        query.setParameter(3, startedAt);
        return query.executeUpdate() == 1;
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
    public void recordTextCommentChange(SurveyAiSummaryTextChangeCommand command) {
        lockSurveyForSourceStateUpdate(command.surveyId());
        if (surveyAiPendingChangeRepository.existsByResponseDetail_Id(command.responseDetailId())) {
            return;
        }

        SurveyAiSourceStateEntity state = surveyAiSourceStateRepository.findById(command.surveyId())
                .orElseGet(() -> newSourceState(command.surveyId()));
        Map<String, Integer> topicCounts = readTopicCounts(state.getTopicCountsJson());
        Map<String, Integer> pendingTopicCounts = readTopicCounts(state.getPendingTopicCountsJson());
        topicCounts.merge(command.topic(), 1, Integer::sum);
        pendingTopicCounts.merge(command.topic(), 1, Integer::sum);

        int nextSourceVersion = value(state.getSourceVersion()) + 1;
        state.setCurrentCommentCount(value(state.getCurrentCommentCount()) + 1);
        state.setPendingCommentCount(value(state.getPendingCommentCount()) + 1);
        state.setPendingScoreSum(value(state.getPendingScoreSum()) + value(command.totalScore()));
        state.setMaxPendingScore(Math.max(value(state.getMaxPendingScore()), value(command.totalScore())));
        state.setTopicCountsJson(writeJson(topicCounts));
        state.setPendingTopicCountsJson(writeJson(pendingTopicCounts));
        state.setCurrentEntropy(calculateEntropy(topicCounts));
        state.setSourceVersion(nextSourceVersion);
        state.setLastChangedAt(command.createdAt());
        surveyAiSourceStateRepository.save(state);

        SurveyAiPendingChangeEntity change = new SurveyAiPendingChangeEntity();
        change.setSurvey(surveyRepository.getReferenceById(command.surveyId()));
        change.setResponseDetail(entityManager.getReference(ResponseDetailEntity.class, command.responseDetailId()));
        change.setQuestion(entityManager.getReference(QuestionEntity.class, command.questionId()));
        change.setCommentLength(value(command.commentLength()));
        change.setTopic(command.topic());
        change.setKeywordScore(value(command.keywordScore()));
        change.setSentimentScore(value(command.sentimentScore()));
        change.setSuggestionScore(value(command.suggestionScore()));
        change.setEntropyImpactScore(value(command.entropyImpactScore()));
        change.setNoveltyScore(value(command.noveltyScore()));
        change.setTotalScore(value(command.totalScore()));
        change.setSourceVersion(nextSourceVersion);
        change.setProcessed(false);
        change.setCreatedAt(command.createdAt());
        surveyAiPendingChangeRepository.save(change);
    }

    @Override
    @Transactional
    public void markSourceStateSummarized(Integer surveyId,
                                          Integer commentCount,
                                          Map<String, Integer> topicCounts,
                                          Integer expectedSourceVersion,
                                          LocalDateTime summarizedAt) {
        lockSurveyForSourceStateUpdate(surveyId);
        SurveyAiSourceStateEntity state = surveyAiSourceStateRepository.findById(surveyId)
                .orElseGet(() -> newSourceState(surveyId));
        Map<String, Integer> safeTopicCounts = topicCounts == null ? Map.of() : topicCounts;
        int summarizedSourceVersion = value(expectedSourceVersion);
        state.setSummarizedCommentCount(value(commentCount));
        state.setSummarizedEntropy(calculateEntropy(safeTopicCounts));
        state.setSummarizedSourceVersion(summarizedSourceVersion);
        state.setLastSummarizedAt(summarizedAt);

        Query query = entityManager.createNativeQuery("""
                UPDATE Survey_AI_Pending_Change
                SET processed = 1
                WHERE survey_id = ?1
                    AND processed = 0
                    AND source_version <= ?2
                """);
        query.setParameter(1, surveyId);
        query.setParameter(2, summarizedSourceVersion);
        query.executeUpdate();

        Map<String, Integer> pendingTopicCounts = applyPendingAggregate(state, surveyId);
        Map<String, Integer> currentTopicCounts = new LinkedHashMap<>(safeTopicCounts);
        pendingTopicCounts.forEach((topic, count) -> currentTopicCounts.merge(topic, count, Integer::sum));
        state.setCurrentCommentCount(Math.max(
                value(state.getCurrentCommentCount()),
                value(commentCount) + value(state.getPendingCommentCount())
        ));
        state.setTopicCountsJson(writeJson(currentTopicCounts));
        state.setCurrentEntropy(calculateEntropy(currentTopicCounts));
        surveyAiSourceStateRepository.save(state);
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

    private SurveyAiSummarySourceStateRecord toSourceStateRecord(SurveyAiSourceStateEntity entity) {
        return new SurveyAiSummarySourceStateRecord(
                entity.getSurveyId(),
                value(entity.getCurrentCommentCount()),
                value(entity.getSummarizedCommentCount()),
                value(entity.getPendingCommentCount()),
                value(entity.getPendingScoreSum()),
                value(entity.getMaxPendingScore()),
                entity.getTopicCountsJson(),
                entity.getPendingTopicCountsJson(),
                value(entity.getCurrentEntropy()),
                value(entity.getSummarizedEntropy()),
                value(entity.getSourceVersion()),
                value(entity.getSummarizedSourceVersion()),
                entity.getLastChangedAt(),
                entity.getLastSummarizedAt()
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

    private String writeJson(Map<String, Integer> values) {
        try {
            return objectMapper.writeValueAsString(values == null ? Map.of() : values);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to serialize AI summary topic counts", exception);
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

    private Map<String, Integer> readTopicCounts(String value) {
        if (value == null || value.isBlank()) {
            return new LinkedHashMap<>();
        }
        try {
            return new LinkedHashMap<>(objectMapper.readValue(value, TOPIC_COUNT_TYPE));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to parse AI summary topic counts", exception);
        }
    }

    private SurveyAiSourceStateEntity newSourceState(Integer surveyId) {
        SurveyAiSourceStateEntity state = new SurveyAiSourceStateEntity();
        state.setSurvey(surveyRepository.getReferenceById(surveyId));
        state.setCurrentCommentCount(0);
        state.setSummarizedCommentCount(0);
        state.setPendingCommentCount(0);
        state.setPendingScoreSum(0);
        state.setMaxPendingScore(0);
        state.setTopicCountsJson(writeJson(Map.of()));
        state.setPendingTopicCountsJson(writeJson(Map.of()));
        state.setCurrentEntropy(0.0d);
        state.setSummarizedEntropy(0.0d);
        state.setSourceVersion(0);
        state.setSummarizedSourceVersion(0);
        return state;
    }

    private Map<String, Integer> applyPendingAggregate(SurveyAiSourceStateEntity state, Integer surveyId) {
        List<SurveyAiPendingChangeEntity> pendingChanges =
                surveyAiPendingChangeRepository.findBySurvey_IdAndProcessedFalse(surveyId);
        Map<String, Integer> pendingTopicCounts = new LinkedHashMap<>();
        int pendingCount = 0;
        int pendingScoreSum = 0;
        int maxPendingScore = 0;
        for (SurveyAiPendingChangeEntity change : pendingChanges) {
            pendingCount++;
            pendingScoreSum += value(change.getTotalScore());
            maxPendingScore = Math.max(maxPendingScore, value(change.getTotalScore()));
            pendingTopicCounts.merge(change.getTopic(), 1, Integer::sum);
        }
        state.setPendingCommentCount(pendingCount);
        state.setPendingScoreSum(pendingScoreSum);
        state.setMaxPendingScore(maxPendingScore);
        state.setPendingTopicCountsJson(writeJson(pendingTopicCounts));
        return pendingTopicCounts;
    }

    private void lockSurveyForSourceStateUpdate(Integer surveyId) {
        Query query = entityManager.createNativeQuery("""
                SELECT survey_id
                FROM [dbo].[Survey] WITH (UPDLOCK, HOLDLOCK)
                WHERE survey_id = ?1
                """);
        query.setParameter(1, surveyId);
        query.getSingleResult();
    }

    private double calculateEntropy(Map<String, Integer> topicCounts) {
        int total = topicCounts.values().stream().mapToInt(this::value).sum();
        if (total <= 0) {
            return 0.0d;
        }
        double entropy = 0.0d;
        for (Integer count : topicCounts.values()) {
            int safeCount = value(count);
            if (safeCount <= 0) {
                continue;
            }
            double probability = (double) safeCount / (double) total;
            entropy -= probability * (Math.log(probability) / Math.log(2));
        }
        return entropy;
    }

    private int value(Integer value) {
        return value == null ? 0 : value;
    }

    private double value(Double value) {
        return value == null ? 0.0d : value;
    }
}
