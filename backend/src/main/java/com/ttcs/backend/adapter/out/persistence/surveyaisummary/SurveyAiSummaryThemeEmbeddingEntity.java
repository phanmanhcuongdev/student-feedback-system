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
@Table(name = "Survey_AI_Summary_Theme_Embedding")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SurveyAiSummaryThemeEmbeddingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "embedding_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "summary_id", nullable = false)
    private SurveyAiSummaryEntity summary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id", nullable = false)
    private SurveyEntity survey;

    @Column(name = "theme_type", nullable = false, length = 30)
    @Nationalized
    private String themeType;

    @Column(name = "theme_index", nullable = false)
    private Integer themeIndex;

    @Column(name = "theme_text", nullable = false, length = 1000)
    @Nationalized
    private String themeText;

    @Column(name = "embedding_json", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    @Nationalized
    private String embeddingJson;

    @Column(name = "model_name", length = 100)
    @Nationalized
    private String modelName;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
