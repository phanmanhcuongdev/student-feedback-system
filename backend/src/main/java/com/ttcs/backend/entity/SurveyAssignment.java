package com.ttcs.backend.entity;

import org.hibernate.annotations.Nationalized;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Survey_Assignment")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SurveyAssignment {
    @Id
    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id")
    private Survey survey;     

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
