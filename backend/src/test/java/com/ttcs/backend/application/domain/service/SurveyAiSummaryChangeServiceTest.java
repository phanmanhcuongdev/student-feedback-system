package com.ttcs.backend.application.domain.service;

import com.ttcs.backend.application.domain.model.Question;
import com.ttcs.backend.application.domain.model.QuestionType;
import com.ttcs.backend.application.domain.model.ResponseDetail;
import com.ttcs.backend.application.domain.model.Survey;
import com.ttcs.backend.application.domain.model.SurveyLifecycleState;
import com.ttcs.backend.application.domain.model.SurveyResponse;
import com.ttcs.backend.application.port.out.GenerateTextEmbeddingPort;
import com.ttcs.backend.application.port.out.LoadSurveyAiSummaryPort;
import com.ttcs.backend.application.port.out.SaveSurveyAiSummaryPort;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SurveyAiSummaryChangeServiceTest {

    private final SaveSurveyAiSummaryPort saveSurveyAiSummaryPort = mock(SaveSurveyAiSummaryPort.class);
    private final LoadSurveyAiSummaryPort loadSurveyAiSummaryPort = mock(LoadSurveyAiSummaryPort.class);
    private final GenerateTextEmbeddingPort generateTextEmbeddingPort = mock(GenerateTextEmbeddingPort.class);
    private final SurveyAiSummaryChangeService service = new SurveyAiSummaryChangeService(
            saveSurveyAiSummaryPort,
            loadSurveyAiSummaryPort,
            generateTextEmbeddingPort,
            new SurveyAiSummaryChangeScorer()
    );

    @Test
    void shouldAddHighNoveltyScoreWhenCommentEmbeddingIsFarFromThemes() {
        when(loadSurveyAiSummaryPort.loadLatestThemeEmbeddings(1)).thenReturn(List.of(themeEmbedding(List.of(1.0d, 0.0d))));
        when(generateTextEmbeddingPort.embed(any())).thenReturn(new GenerateTextEmbeddingPort.TextEmbeddingResult(
                "embedding-test",
                List.of(List.of(0.0d, 1.0d))
        ));

        service.recordSubmittedTextComments(List.of(textDetail("Can bo sung noi dung moi ve du an thuc te.")));

        ArgumentCaptor<SaveSurveyAiSummaryPort.SurveyAiSummaryTextChangeCommand> captor =
                ArgumentCaptor.forClass(SaveSurveyAiSummaryPort.SurveyAiSummaryTextChangeCommand.class);
        verify(saveSurveyAiSummaryPort).recordTextCommentChange(captor.capture());
        assertEquals(4, captor.getValue().noveltyScore());
    }

    @Test
    void shouldFallbackToRuleBasedScoreWhenEmbeddingFails() {
        when(loadSurveyAiSummaryPort.loadLatestThemeEmbeddings(1)).thenReturn(List.of(themeEmbedding(List.of(1.0d, 0.0d))));
        when(generateTextEmbeddingPort.embed(any())).thenThrow(new IllegalStateException("local embedding unavailable"));

        service.recordSubmittedTextComments(List.of(textDetail("Can bo sung noi dung moi ve du an thuc te.")));

        ArgumentCaptor<SaveSurveyAiSummaryPort.SurveyAiSummaryTextChangeCommand> captor =
                ArgumentCaptor.forClass(SaveSurveyAiSummaryPort.SurveyAiSummaryTextChangeCommand.class);
        verify(saveSurveyAiSummaryPort).recordTextCommentChange(captor.capture());
        assertEquals(0, captor.getValue().noveltyScore());
    }

    private ResponseDetail textDetail(String comment) {
        Survey survey = new Survey(
                1,
                "Course survey",
                "Description",
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                99,
                false,
                SurveyLifecycleState.PUBLISHED
        );
        SurveyResponse response = new SurveyResponse(10, null, null, survey, LocalDateTime.now());
        Question question = new Question(20, 1, "Comment", QuestionType.TEXT);
        return new ResponseDetail(30, response, question, null, comment);
    }

    private LoadSurveyAiSummaryPort.SurveyAiSummaryThemeEmbeddingRecord themeEmbedding(List<Double> vector) {
        return new LoadSurveyAiSummaryPort.SurveyAiSummaryThemeEmbeddingRecord(
                1,
                2,
                1,
                "SUMMARY",
                0,
                "Existing theme",
                vector,
                "embedding-test",
                LocalDateTime.now()
        );
    }
}
