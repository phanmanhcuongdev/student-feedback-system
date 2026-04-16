package com.ttcs.backend.adapter.out.persistence.surveyrecipient;

import com.ttcs.backend.adapter.out.persistence.student.StudentEntity;
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

import java.time.LocalDateTime;

@Entity
@Table(name = "Survey_Recipient")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SurveyRecipientEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recipient_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "survey_id", nullable = false)
    private SurveyEntity survey;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentEntity student;

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;

    @Column(name = "opened_at")
    private LocalDateTime openedAt;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;
}
