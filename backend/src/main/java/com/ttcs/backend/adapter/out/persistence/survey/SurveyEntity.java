package com.ttcs.backend.adapter.out.persistence.survey;

import java.time.LocalDateTime;

import com.ttcs.backend.adapter.out.persistence.admin.AdminEntity;
import lombok.*;
import org.hibernate.annotations.Nationalized;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "Survey")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SurveyEntity {
    @Id
    @Column(name = "survey_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "title", nullable = false)
    @Nationalized
    private String title;

    @Column(name = "title_vi", columnDefinition = "NVARCHAR(MAX)")
    @Nationalized
    private String titleVi;

    @Column(name = "title_en", columnDefinition = "NVARCHAR(MAX)")
    @Nationalized
    private String titleEn;

    @Nationalized
    @Column(name = "description", columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Nationalized
    @Column(name = "description_vi", columnDefinition = "NVARCHAR(MAX)")
    private String descriptionVi;

    @Nationalized
    @Column(name = "description_en", columnDefinition = "NVARCHAR(MAX)")
    private String descriptionEn;

    @Column(name = "source_lang", length = 10)
    private String sourceLang;

    @Column(name = "is_auto_translated", nullable = false)
    private boolean autoTranslated;

    @Column(name = "model_info", length = 100)
    private String modelInfo;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "hidden", nullable = false)
    private boolean hidden;

    @Column(name = "lifecycle_state", nullable = false)
    private String lifecycleState;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private AdminEntity createdBy;

    public SurveyEntity(
            Integer id,
            String title,
            String description,
            LocalDateTime startDate,
            LocalDateTime endDate,
            boolean hidden,
            String lifecycleState,
            AdminEntity createdBy
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.hidden = hidden;
        this.lifecycleState = lifecycleState;
        this.createdBy = createdBy;
    }

}
