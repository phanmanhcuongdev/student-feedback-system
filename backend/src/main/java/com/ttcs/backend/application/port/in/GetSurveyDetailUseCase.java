package com.ttcs.backend.application.port.in;

import com.ttcs.backend.adapter.in.web.dto.SurveyDetailResponse;
import com.ttcs.backend.application.domain.model.Question;

import java.util.List;

public interface GetSurveyDetailUseCase {
    SurveyDetailResponse getSurveyDetail(Integer surveyId);
}