package com.ttcs.backend.adapter.out.persistence;


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
@Table(name = "Question")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuestionEntity {
    
    @Id
    @Column(name = "question_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id", nullable = false)
    private SurveyEntity survey;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    @Nationalized
    private String content;

    @Column(length = 20)
    @Nationalized
    private String type;
}
