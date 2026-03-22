package com.ttcs.backend.adapter.in.web.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubmitSurveyAnswerRequest {
    private Integer questionId;
    private Integer rating;
    private String comment;
}