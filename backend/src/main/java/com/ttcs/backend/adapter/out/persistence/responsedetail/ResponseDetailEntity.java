package com.ttcs.backend.adapter.out.persistence.responsedetail;

import com.ttcs.backend.adapter.out.persistence.question.QuestionEntity;
import com.ttcs.backend.adapter.out.persistence.surveyresponse.SurveyResponseEntity;
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
@Table(name = "Response_Detail")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResponseDetailEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "response_id", nullable = false)
    private SurveyResponseEntity response;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private QuestionEntity question;

    private Integer rating;

    @Nationalized
    @Column(name = "comment", columnDefinition = "NVARCHAR(MAX)")
    private String comment;
}
