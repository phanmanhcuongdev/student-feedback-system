package com.ttcs.backend.application.port.out;

import com.ttcs.backend.application.port.in.resultview.ExportedReport;
import com.ttcs.backend.application.port.in.resultview.SurveyReportView;

public interface SurveyReportRenderer {

    ExportedReport render(SurveyReportView report);
}
