package com.ttcs.backend.adapter.out.persistence.surveytemplate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

@Entity
@Table(name = "Survey_Template_Question")
@Getter
@Setter
public class SurveyTemplateQuestionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "template_question_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private SurveyTemplateEntity template;

    @Column(name = "question_bank_id")
    private Integer questionBankEntryId;

    @Nationalized
    @Column(name = "content", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String content;

    @Nationalized
    @Column(name = "type", nullable = false, length = 20)
    private String type;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;
}
