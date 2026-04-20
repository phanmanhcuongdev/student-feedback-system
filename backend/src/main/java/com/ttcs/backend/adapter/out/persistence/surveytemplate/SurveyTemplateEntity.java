package com.ttcs.backend.adapter.out.persistence.surveytemplate;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Survey_Template")
@Getter
@Setter
public class SurveyTemplateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "template_id")
    private Integer id;

    @Nationalized
    @Column(name = "name", nullable = false)
    private String name;

    @Nationalized
    @Column(name = "description", columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Nationalized
    @Column(name = "suggested_title")
    private String suggestedTitle;

    @Nationalized
    @Column(name = "suggested_survey_description", columnDefinition = "NVARCHAR(MAX)")
    private String suggestedSurveyDescription;

    @Column(name = "recipient_scope", nullable = false)
    private String recipientScope;

    @Column(name = "recipient_department_id")
    private Integer recipientDepartmentId;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("displayOrder ASC, id ASC")
    private List<SurveyTemplateQuestionEntity> questions = new ArrayList<>();
}
