package com.ttcs.backend.adapter.out.persistence.surveyassignment;

import com.ttcs.backend.adapter.out.persistence.survey.SurveyEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Nationalized;

@Entity
@Table(name = "Survey_Assignment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SurveyAssignmentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id", nullable = false)
    private SurveyEntity survey;

    @Column(name = "evaluator_type", nullable = false)
    @Nationalized
    private String evaluatorType;

    @Column(name = "evaluator_value")
    private Integer evaluatorValue;

    @Column(name = "subject_type", nullable = false)
    @Nationalized
    private String subjectType;

    @Column(name = "subject_value")
    private Integer subjectValue; 

}
