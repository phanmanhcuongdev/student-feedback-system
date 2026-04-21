package com.ttcs.backend.adapter.out.persistence.surveyaisummary;

import com.ttcs.backend.adapter.out.persistence.survey.SurveyEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDateTime;

@Entity
@Table(name = "Survey_AI_Summary")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SurveyAiSummaryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "summary_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id", nullable = false)
    private SurveyEntity survey;

    @Column(name = "source_hash", nullable = false, length = 64)
    private String sourceHash;

    @Column(name = "model_name", nullable = false, length = 100)
    @Nationalized
    private String modelName;

    @Column(name = "comment_count", nullable = false)
    private Integer commentCount;

    @Column(name = "summary_text", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    @Nationalized
    private String summaryText;

    @Column(name = "highlights_json", columnDefinition = "NVARCHAR(MAX)")
    @Nationalized
    private String highlightsJson;

    @Column(name = "concerns_json", columnDefinition = "NVARCHAR(MAX)")
    @Nationalized
    private String concernsJson;

    @Column(name = "actions_json", columnDefinition = "NVARCHAR(MAX)")
    @Nationalized
    private String actionsJson;

    @Column(name = "created_by_user_id", nullable = false)
    private Integer createdByUserId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
