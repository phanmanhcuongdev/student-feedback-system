package com.ttcs.backend.application.domain.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SurveyAiSummaryChangeScorerTest {

    private final SurveyAiSummaryChangeScorer scorer = new SurveyAiSummaryChangeScorer();

    @Test
    void shouldScoreLongSuggestionWithProblemKeywords() {
        var result = scorer.score("Phong hoc bi hong may chieu va wifi khong hoat dong, can cai thien som.");

        assertEquals("FACILITY_ISSUE", result.topic());
        assertEquals(4, result.keywordScore());
        assertEquals(0, result.sentimentScore());
        assertEquals(2, result.suggestionScore());
        assertEquals(8, result.totalScore());
    }

    @Test
    void shouldScoreStrongNegativeTeachingComment() {
        var result = scorer.score("Bai giang kho hieu, sinh vien qua tai va mong muon co them vi du thuc hanh.");

        assertEquals("PRACTICE_REQUEST", result.topic());
        assertEquals(5, result.keywordScore());
        assertEquals(2, result.sentimentScore());
        assertEquals(2, result.suggestionScore());
        assertEquals(11, result.totalScore());
    }

    @Test
    void shouldClassifyCourseContentTopic() {
        var result = scorer.score("Noi dung mon hoc va tai lieu can duoc bo sung them.");

        assertEquals("COURSE_CONTENT", result.topic());
        assertEquals(2, result.keywordScore());
        assertEquals(2, result.suggestionScore());
    }

    @Test
    void shouldNormalizeVietnameseDCharacter() {
        var result = scorer.score("Điểm kiểm tra cần có đáp án rõ hơn.");

        assertEquals("ASSESSMENT_ISSUE", result.topic());
        assertEquals(2, result.keywordScore());
        assertEquals(2, result.suggestionScore());
    }
}
