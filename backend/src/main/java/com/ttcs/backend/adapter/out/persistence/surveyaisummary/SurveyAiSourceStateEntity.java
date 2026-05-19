package com.ttcs.backend.adapter.out.persistence.surveyaisummary;

import com.ttcs.backend.adapter.out.persistence.survey.SurveyEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDateTime;

@Entity
@Table(name = "Survey_AI_Source_State")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SurveyAiSourceStateEntity {
    @Id
    @Column(name = "survey_id")
    private Integer surveyId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "survey_id")
    private SurveyEntity survey;

    @Column(name = "current_comment_count", nullable = false)
    private Integer currentCommentCount;

    @Column(name = "summarized_comment_count", nullable = false)
    private Integer summarizedCommentCount;

    @Column(name = "pending_comment_count", nullable = false)
    private Integer pendingCommentCount;

    @Column(name = "pending_score_sum", nullable = false)
    private Integer pendingScoreSum;

    @Column(name = "max_pending_score", nullable = false)
    private Integer maxPendingScore;

    @Column(name = "topic_counts_json", columnDefinition = "NVARCHAR(MAX)")
    @Nationalized
    private String topicCountsJson;

    @Column(name = "pending_topic_counts_json", columnDefinition = "NVARCHAR(MAX)")
    @Nationalized
    private String pendingTopicCountsJson;

    @Column(name = "current_entropy", nullable = false)
    private Double currentEntropy;

    @Column(name = "summarized_entropy", nullable = false)
    private Double summarizedEntropy;

    @Column(name = "source_version", nullable = false)
    private Integer sourceVersion;

    @Column(name = "summarized_source_version", nullable = false)
    private Integer summarizedSourceVersion;

    @Column(name = "last_changed_at")
    private LocalDateTime lastChangedAt;

    @Column(name = "last_summarized_at")
    private LocalDateTime lastSummarizedAt;
}
