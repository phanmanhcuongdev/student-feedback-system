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
@Table(name = "Survey_AI_Summary_Job")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SurveyAiSummaryJobEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "job_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id", nullable = false)
    private SurveyEntity survey;

    @Column(name = "source_hash", nullable = false, length = 64)
    private String sourceHash;

    @Column(name = "comment_count", nullable = false)
    private Integer commentCount;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "requested_by_user_id", nullable = false)
    private Integer requestedByUserId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "summary_id")
    private SurveyAiSummaryEntity summary;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @Column(name = "error_message", columnDefinition = "NVARCHAR(MAX)")
    @Nationalized
    private String errorMessage;
}
