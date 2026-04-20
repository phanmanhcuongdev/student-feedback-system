package com.ttcs.backend.adapter.out.persistence.surveyresponse;

import java.time.LocalDateTime;

import com.ttcs.backend.adapter.out.persistence.student.StudentEntity;
import com.ttcs.backend.adapter.out.persistence.survey.SurveyEntity;
import com.ttcs.backend.adapter.out.persistence.lecturer.LecturerEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "Survey_Response")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SurveyResponseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "response_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private StudentEntity student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecturer_id")
    private LecturerEntity lecturer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id", nullable = false)
    private SurveyEntity survey;

    @Column(name = "submitted_at", nullable = false, updatable = false)
    @org.hibernate.annotations.CreationTimestamp // tu dong dien thoi gian he thong
    private LocalDateTime submittedAt;
}
