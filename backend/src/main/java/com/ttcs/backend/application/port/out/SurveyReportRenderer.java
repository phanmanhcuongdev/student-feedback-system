package com.ttcs.backend.application.port.out;

public interface SurveyReportRenderer {

    RenderedReport render(SurveyReport report, String format);
}
