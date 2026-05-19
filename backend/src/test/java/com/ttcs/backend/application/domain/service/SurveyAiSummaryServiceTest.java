package com.ttcs.backend.application.domain.service;

import com.ttcs.backend.application.domain.model.Role;
import com.ttcs.backend.application.domain.model.Survey;
import com.ttcs.backend.application.domain.model.SurveyAiSummaryJobStatus;
import com.ttcs.backend.application.domain.model.SurveyLifecycleState;
import com.ttcs.backend.application.port.out.GenerateSurveyCommentSummaryPort;
import com.ttcs.backend.application.port.out.GenerateTextEmbeddingPort;
import com.ttcs.backend.application.port.out.LoadLecturerByUserIdPort;
import com.ttcs.backend.application.port.out.LoadSurveyAiSummaryPort;
import com.ttcs.backend.application.port.out.LoadSurveyAssignmentPort;
import com.ttcs.backend.application.port.out.LoadSurveyPort;
import com.ttcs.backend.application.port.out.ScheduleSurveyAiSummaryJobPort;
import com.ttcs.backend.application.port.out.SaveSurveyAiSummaryPort;
import com.ttcs.backend.application.port.out.SurveyCommentSummaryResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SurveyAiSummaryServiceTest {

    private final LoadSurveyPort loadSurveyPort = mock(LoadSurveyPort.class);
    private final LoadSurveyAssignmentPort loadSurveyAssignmentPort = mock(LoadSurveyAssignmentPort.class);
    private final LoadLecturerByUserIdPort loadLecturerByUserIdPort = mock(LoadLecturerByUserIdPort.class);
    private final LoadSurveyAiSummaryPort loadSurveyAiSummaryPort = mock(LoadSurveyAiSummaryPort.class);
    private final SaveSurveyAiSummaryPort saveSurveyAiSummaryPort = mock(SaveSurveyAiSummaryPort.class);
    private final GenerateSurveyCommentSummaryPort generateSurveyCommentSummaryPort = mock(GenerateSurveyCommentSummaryPort.class);
    private final GenerateTextEmbeddingPort generateTextEmbeddingPort = mock(GenerateTextEmbeddingPort.class);

    private SurveyAiSummaryService service;

    @BeforeEach
    void setUp() {
        AtomicReference<SurveyAiSummaryService> serviceRef = new AtomicReference<>();
        ScheduleSurveyAiSummaryJobPort scheduleSurveyAiSummaryJobPort = command -> serviceRef.get().processJob(command);
        service = new SurveyAiSummaryService(
                loadSurveyPort,
                loadSurveyAssignmentPort,
                loadLecturerByUserIdPort,
                loadSurveyAiSummaryPort,
                generateSurveyCommentSummaryPort,
                generateTextEmbeddingPort,
                saveSurveyAiSummaryPort,
                scheduleSurveyAiSummaryJobPort,
                new SurveyAiSummaryChangeScorer()
        );
        serviceRef.set(service);
    }

    @Test
    void shouldGenerateNewSummarySuccessfully() {
        givenSurveyAndPayload();
        when(loadSurveyAiSummaryPort.loadLatestSummary(1)).thenReturn(Optional.empty());
        when(loadSurveyAiSummaryPort.loadLatestJob(1)).thenReturn(Optional.empty());
        when(loadSurveyAiSummaryPort.loadActiveJob(1)).thenReturn(Optional.empty());
        when(saveSurveyAiSummaryPort.createJob(1, sourceHash(), 1, 99)).thenReturn(job(10, SurveyAiSummaryJobStatus.QUEUED));
        when(saveSurveyAiSummaryPort.markJobProcessingIfNoActiveJob(eq(10), eq(1), any(LocalDateTime.class))).thenReturn(true);
        when(generateSurveyCommentSummaryPort.generateSummary(any())).thenReturn(new SurveyCommentSummaryResult(
                "gemini-test",
                "Students liked the course.",
                List.of("Clear lectures"),
                List.of("More practice needed"),
                List.of("Add exercises")
        ));
        when(generateTextEmbeddingPort.embed(any())).thenReturn(new GenerateTextEmbeddingPort.TextEmbeddingResult(
                "embedding-test",
                List.of(List.of(1.0d), List.of(0.9d), List.of(0.8d), List.of(0.7d))
        ));
        when(saveSurveyAiSummaryPort.saveSummary(
                eq(1),
                eq(sourceHash()),
                eq("gemini-test"),
                eq(1),
                eq("Students liked the course."),
                eq(List.of("Clear lectures")),
                eq(List.of("More practice needed")),
                eq(List.of("Add exercises")),
                eq(99)
        )).thenReturn(summary(100));

        var result = service.generate(1, 99, Role.ADMIN);

        assertEquals("QUEUED", result.status());
        assertEquals(10, result.jobId());
        verify(saveSurveyAiSummaryPort).markJobProcessingIfNoActiveJob(eq(10), eq(1), any(LocalDateTime.class));
        verify(generateSurveyCommentSummaryPort).generateSummary(any());
        verify(saveSurveyAiSummaryPort).markJobCompleted(eq(10), eq(100), any(LocalDateTime.class));
        verify(saveSurveyAiSummaryPort).saveThemeEmbeddings(any());
    }

    @Test
    void shouldSkipWhenActiveJobAlreadyExists() {
        givenSurveyAndPayload();
        when(loadSurveyAiSummaryPort.loadLatestSummary(1)).thenReturn(Optional.empty());
        when(loadSurveyAiSummaryPort.loadLatestJob(1)).thenReturn(Optional.empty());
        when(loadSurveyAiSummaryPort.loadActiveJob(1)).thenReturn(Optional.of(job(11, SurveyAiSummaryJobStatus.PROCESSING)));

        var result = service.generate(1, 99, Role.ADMIN);

        assertEquals("PROCESSING", result.status());
        assertEquals(11, result.jobId());
        verify(saveSurveyAiSummaryPort, never()).createJob(any(), any(), any(), any());
        verify(generateSurveyCommentSummaryPort, never()).generateSummary(any());
    }

    @Test
    void shouldMarkJobFailedWhenAiPortThrowsException() {
        givenSurveyAndPayload();
        when(loadSurveyAiSummaryPort.loadLatestSummary(1)).thenReturn(Optional.empty());
        when(loadSurveyAiSummaryPort.loadLatestJob(1)).thenReturn(Optional.empty());
        when(loadSurveyAiSummaryPort.loadActiveJob(1)).thenReturn(Optional.empty());
        when(saveSurveyAiSummaryPort.createJob(1, sourceHash(), 1, 99)).thenReturn(job(12, SurveyAiSummaryJobStatus.QUEUED));
        when(saveSurveyAiSummaryPort.markJobProcessingIfNoActiveJob(eq(12), eq(1), any(LocalDateTime.class))).thenReturn(true);
        when(generateSurveyCommentSummaryPort.generateSummary(any())).thenThrow(new IllegalStateException("provider timed out"));

        var result = service.generate(1, 99, Role.ADMIN);

        assertEquals("QUEUED", result.status());
        assertEquals(12, result.jobId());
        verify(saveSurveyAiSummaryPort).markJobFailed(eq(12), eq("IllegalStateException: provider timed out"), any(LocalDateTime.class));
        verify(saveSurveyAiSummaryPort, never()).markJobCompleted(any(), any(), any());
    }

    @Test
    void shouldReuseExistingSummaryWithoutLoadingPayloadWhenPendingChangesAreMinor() {
        givenSurveyOnly();
        when(loadSurveyAiSummaryPort.loadLatestSummary(1)).thenReturn(Optional.of(summary(100)));
        when(loadSurveyAiSummaryPort.loadSourceState(1)).thenReturn(Optional.of(sourceState(2, 3, 2, 0.01d, 12)));

        var result = service.generate(1, 99, Role.ADMIN);

        assertEquals("COMPLETED", result.status());
        assertEquals("Students liked the course.", result.summary());
        assertEquals(true, result.stale());
        assertEquals(false, result.refreshRecommended());
        verify(loadSurveyAiSummaryPort, never()).loadSurveySummaryPayload(1);
        verify(saveSurveyAiSummaryPort, never()).createJob(any(), any(), any(), any());
        verify(generateSurveyCommentSummaryPort, never()).generateSummary(any());
    }

    @Test
    void shouldRefreshExistingSummaryWhenPendingChangesAreMeaningful() {
        givenSurveyAndPayload();
        when(loadSurveyAiSummaryPort.loadLatestSummary(1)).thenReturn(Optional.of(summary(100, "old-source-hash")));
        when(loadSurveyAiSummaryPort.loadSourceState(1)).thenReturn(Optional.of(sourceState(10, 15, 7, 0.2d, 42)));
        when(loadSurveyAiSummaryPort.loadLatestJob(1)).thenReturn(Optional.empty());
        when(loadSurveyAiSummaryPort.loadActiveJob(1)).thenReturn(Optional.empty());
        when(saveSurveyAiSummaryPort.createJob(1, sourceHash(), 1, 99)).thenReturn(job(13, SurveyAiSummaryJobStatus.QUEUED));
        when(saveSurveyAiSummaryPort.markJobProcessingIfNoActiveJob(eq(13), eq(1), any(LocalDateTime.class))).thenReturn(false);

        var result = service.generate(1, 99, Role.ADMIN);

        assertEquals("QUEUED", result.status());
        assertEquals(13, result.jobId());
        verify(loadSurveyAiSummaryPort).loadSurveySummaryPayload(1);
        verify(saveSurveyAiSummaryPort).createJob(1, sourceHash(), 1, 99);
    }

    @Test
    void shouldRefreshExistingSummaryWhenNewImportantTopicAppearsOftenEnough() {
        givenSurveyAndPayload();
        when(loadSurveyAiSummaryPort.loadLatestSummary(1)).thenReturn(Optional.of(summary(100, "old-source-hash")));
        when(loadSurveyAiSummaryPort.loadSourceState(1)).thenReturn(Optional.of(sourceState(3, 3, 1, 0.01d, 42, 3)));
        when(loadSurveyAiSummaryPort.loadLatestJob(1)).thenReturn(Optional.empty());
        when(loadSurveyAiSummaryPort.loadActiveJob(1)).thenReturn(Optional.empty());
        when(saveSurveyAiSummaryPort.createJob(1, sourceHash(), 1, 99)).thenReturn(job(14, SurveyAiSummaryJobStatus.QUEUED));
        when(saveSurveyAiSummaryPort.markJobProcessingIfNoActiveJob(eq(14), eq(1), any(LocalDateTime.class))).thenReturn(false);

        var result = service.generate(1, 99, Role.ADMIN);

        assertEquals("QUEUED", result.status());
        assertEquals(14, result.jobId());
        verify(loadSurveyAiSummaryPort).loadSurveySummaryPayload(1);
        verify(saveSurveyAiSummaryPort).createJob(1, sourceHash(), 1, 99);
    }

    @Test
    void shouldRebuildSourceStateWhenTrackingStateIsMissing() {
        givenSurveyAndPayload();
        when(loadSurveyAiSummaryPort.loadLatestJob(1)).thenReturn(Optional.empty());
        when(loadSurveyAiSummaryPort.loadLatestSummary(1)).thenReturn(Optional.empty());
        when(loadSurveyAiSummaryPort.loadSourceState(1)).thenReturn(Optional.empty());

        var result = service.getSummary(1, 99, Role.ADMIN);

        assertEquals("NOT_REQUESTED", result.status());
        assertEquals(1, result.pendingCommentCount());
        verify(saveSurveyAiSummaryPort).rebuildSourceState(argThat(command ->
                command.surveyId().equals(1)
                        && command.currentCommentCount().equals(1)
                        && command.summarizedCommentCount().equals(0)
        ));
    }

    private void givenSurveyAndPayload() {
        givenSurveyOnly();
        when(loadSurveyAiSummaryPort.loadSurveySummaryPayload(1)).thenReturn(new LoadSurveyAiSummaryPort.SurveyAiSummaryPayload(
                1,
                "Course survey",
                1,
                List.of(new LoadSurveyAiSummaryPort.QuestionCommentPayload(100, "What went well?", "Lectures were clear."))
        ));
    }

    private void givenSurveyOnly() {
        when(loadSurveyPort.loadById(1)).thenReturn(Optional.of(new Survey(
                1,
                "Course survey",
                "Description",
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                99,
                false,
                SurveyLifecycleState.PUBLISHED
        )));
    }

    private String sourceHash() {
        return "39860a354f77af436a6965152832ca83397ebe9a4bc98ddcc7ec13702b3ba3fa";
    }

    private LoadSurveyAiSummaryPort.SurveyAiSummaryJobRecord job(Integer id, SurveyAiSummaryJobStatus status) {
        LocalDateTime now = LocalDateTime.now();
        return new LoadSurveyAiSummaryPort.SurveyAiSummaryJobRecord(
                id,
                1,
                sourceHash(),
                1,
                status,
                99,
                null,
                now,
                status == SurveyAiSummaryJobStatus.PROCESSING ? now : null,
                null,
                null
        );
    }

    private LoadSurveyAiSummaryPort.SurveyAiSummaryRecord summary(Integer id) {
        return summary(id, sourceHash());
    }

    private LoadSurveyAiSummaryPort.SurveyAiSummaryRecord summary(Integer id, String sourceHash) {
        return new LoadSurveyAiSummaryPort.SurveyAiSummaryRecord(
                id,
                1,
                sourceHash,
                "gemini-test",
                1,
                "Students liked the course.",
                List.of("Clear lectures"),
                List.of("More practice needed"),
                List.of("Add exercises"),
                99,
                LocalDateTime.now()
        );
    }

    private LoadSurveyAiSummaryPort.SurveyAiSummarySourceStateRecord sourceState(int pendingCount,
                                                                                 int pendingScore,
                                                                                 int maxPendingScore,
                                                                                 double entropyDelta,
                                                                                 int sourceVersion) {
        return new LoadSurveyAiSummaryPort.SurveyAiSummarySourceStateRecord(
                1,
                100 + pendingCount,
                100,
                pendingCount,
                pendingScore,
                maxPendingScore,
                "{}",
                "{}",
                1.0d + entropyDelta,
                1.0d,
                0,
                sourceVersion,
                sourceVersion - pendingCount,
                LocalDateTime.now(),
                LocalDateTime.now().minusHours(1)
        );
    }

    private LoadSurveyAiSummaryPort.SurveyAiSummarySourceStateRecord sourceState(int pendingCount,
                                                                                 int pendingScore,
                                                                                 int maxPendingScore,
                                                                                 double entropyDelta,
                                                                                 int sourceVersion,
                                                                                 int importantPendingTopicCount) {
        return new LoadSurveyAiSummaryPort.SurveyAiSummarySourceStateRecord(
                1,
                100 + pendingCount,
                100,
                pendingCount,
                pendingScore,
                maxPendingScore,
                "{}",
                "{}",
                1.0d + entropyDelta,
                1.0d,
                importantPendingTopicCount,
                sourceVersion,
                sourceVersion - pendingCount,
                LocalDateTime.now(),
                LocalDateTime.now().minusHours(1)
        );
    }

}
