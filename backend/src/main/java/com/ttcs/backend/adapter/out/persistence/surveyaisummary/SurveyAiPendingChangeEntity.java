package com.ttcs.backend.adapter.out.persistence.surveyaisummary;

import com.ttcs.backend.adapter.out.persistence.question.QuestionEntity;
import com.ttcs.backend.adapter.out.persistence.responsedetail.ResponseDetailEntity;
import com.ttcs.backend.adapter.out.persistence.survey.SurveyEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDateTime;

@Entity
@Table(name = "Survey_AI_Pending_Change")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SurveyAiPendingChangeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "change_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id", nullable = false)
    private SurveyEntity survey;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "response_detail_id", nullable = false)
    private ResponseDetailEntity responseDetail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private QuestionEntity question;

    @Column(name = "comment_length", nullable = false)
    private Integer commentLength;

    @Column(name = "topic", nullable = false, length = 50)
    @Nationalized
    private String topic;

    @Column(name = "keyword_score", nullable = false)
    private Integer keywordScore;

    @Column(name = "sentiment_score", nullable = false)
    private Integer sentimentScore;

    @Column(name = "suggestion_score", nullable = false)
    private Integer suggestionScore;

    @Column(name = "entropy_impact_score", nullable = false)
    private Integer entropyImpactScore;

    @Column(name = "novelty_score", nullable = false)
    private Integer noveltyScore;

    @Column(name = "total_score", nullable = false)
    private Integer totalScore;

    @Column(name = "source_version", nullable = false)
    private Integer sourceVersion;

    @Column(name = "processed", nullable = false)
    private boolean processed;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
