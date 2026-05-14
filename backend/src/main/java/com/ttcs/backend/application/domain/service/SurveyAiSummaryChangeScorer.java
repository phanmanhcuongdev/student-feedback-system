package com.ttcs.backend.application.domain.service;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Set;

@Component
public class SurveyAiSummaryChangeScorer {

    private static final Set<String> NEGATIVE_KEYWORDS = Set.of(
            "khong hieu", "kho hieu", "te", "cham", "thieu",
            "khong cong bang", "qua tai", "bat cap", "gian doan"
    );
    private static final Set<String> PROBLEM_KEYWORDS = Set.of(
            "loi", "hong", "khong hoat dong"
    );
    private static final Set<String> SUGGESTION_KEYWORDS = Set.of(
            "nen", "can", "mong muon", "de nghi", "cai thien", "bo sung", "them"
    );
    private static final Set<String> FACILITY_KEYWORDS = Set.of(
            "phong hoc", "may chieu", "wifi", "thiet bi", "co so vat chat", "nong", "am thanh"
    );
    private static final Set<String> PRACTICE_KEYWORDS = Set.of(
            "thuc hanh", "bai tap", "vi du", "bai mau", "luyen tap", "demo"
    );
    private static final Set<String> ASSESSMENT_KEYWORDS = Set.of(
            "diem", "thi", "kiem tra", "danh gia", "dap an", "rubric"
    );
    private static final Set<String> COURSE_CONTENT_KEYWORDS = Set.of(
            "noi dung", "giao trinh", "chuong trinh", "tai lieu", "kien thuc",
            "mon hoc", "de cuong", "bai hoc"
    );
    private static final Set<String> TEACHING_KEYWORDS = Set.of(
            "giang vien", "day", "giai thich", "bai giang", "truyen dat"
    );

    public ScoredChange score(String comment) {
        return score(comment, 0);
    }

    public ScoredChange score(String comment, int noveltyScore) {
        String normalized = normalize(comment);
        String topic = classifyNormalizedTopic(normalized);
        int keywordScore = keywordScore(normalized);
        int sentimentScore = sentimentScore(normalized);
        int suggestionScore = suggestionScore(normalized);
        int entropyImpactScore = "OTHER".equals(topic) ? 0 : 1;
        int totalScore = lengthScore(comment)
                + keywordScore
                + sentimentScore
                + suggestionScore
                + entropyImpactScore
                + noveltyScore;
        return new ScoredChange(
                topic,
                keywordScore,
                sentimentScore,
                suggestionScore,
                entropyImpactScore,
                noveltyScore,
                totalScore
        );
    }

    public String classifyTopic(String comment) {
        return classifyNormalizedTopic(normalize(comment));
    }

    private int lengthScore(String comment) {
        int length = comment == null ? 0 : comment.strip().length();
        if (length >= 120) {
            return 2;
        }
        return length >= 50 ? 1 : 0;
    }

    private int keywordScore(String normalized) {
        int score = 0;
        if (containsAny(normalized, NEGATIVE_KEYWORDS)) {
            score += 3;
        }
        if (containsAny(normalized, PROBLEM_KEYWORDS)) {
            score += 2;
        }
        if (containsAny(normalized, FACILITY_KEYWORDS)) {
            score += 2;
        }
        if (containsAny(normalized, PRACTICE_KEYWORDS)) {
            score += 2;
        }
        if (containsAny(normalized, ASSESSMENT_KEYWORDS)) {
            score += 2;
        }
        if (containsAny(normalized, COURSE_CONTENT_KEYWORDS)) {
            score += 2;
        }
        return score;
    }

    private int sentimentScore(String normalized) {
        return containsAny(normalized, NEGATIVE_KEYWORDS) ? 2 : 0;
    }

    private int suggestionScore(String normalized) {
        return containsAny(normalized, SUGGESTION_KEYWORDS) ? 2 : 0;
    }

    private String classifyNormalizedTopic(String normalized) {
        if (containsAny(normalized, FACILITY_KEYWORDS)) {
            return "FACILITY_ISSUE";
        }
        if (containsAny(normalized, PRACTICE_KEYWORDS)) {
            return "PRACTICE_REQUEST";
        }
        if (containsAny(normalized, ASSESSMENT_KEYWORDS)) {
            return "ASSESSMENT_ISSUE";
        }
        if (containsAny(normalized, COURSE_CONTENT_KEYWORDS)) {
            return "COURSE_CONTENT";
        }
        if (containsAny(normalized, TEACHING_KEYWORDS)) {
            return "TEACHING_QUALITY";
        }
        return "OTHER";
    }

    private boolean containsAny(String value, Set<String> keywords) {
        return keywords.stream().anyMatch(keyword -> containsKeyword(value, keyword));
    }

    private boolean containsKeyword(String value, String keyword) {
        if (keyword.contains(" ")) {
            return value.contains(keyword);
        }
        return (" " + value + " ").contains(" " + keyword + " ");
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String noMarks = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return noMarks.toLowerCase(Locale.ROOT)
                .replace('\u0111', 'd')
                .replaceAll("[^\\p{Alnum}\\s]", " ")
                .replaceAll("\\s+", " ")
                .strip();
    }

    public record ScoredChange(
            String topic,
            Integer keywordScore,
            Integer sentimentScore,
            Integer suggestionScore,
            Integer entropyImpactScore,
            Integer noveltyScore,
            Integer totalScore
    ) {
    }
}
