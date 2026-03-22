package com.ttcs.backend.adapter.in.web.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SubmitSurveyRequest {
    private Integer studentId;
    private Integer surveyId;
    private List<SubmitSurveyAnswerRequest> answers;
}