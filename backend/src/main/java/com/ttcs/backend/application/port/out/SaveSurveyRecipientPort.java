package com.ttcs.backend.application.port.out;

import com.ttcs.backend.application.domain.model.SurveyRecipient;

import java.util.List;

public interface SaveSurveyRecipientPort {
    SurveyRecipient save(SurveyRecipient recipient);

    List<SurveyRecipient> saveAll(List<SurveyRecipient> recipients);

    /**
     * Bulk insert recipients for a survey using a single SQL statement.
     * @param surveyId the survey ID
     * @param departmentId if not null, only students of this department; if null, all active students
     * @return number of recipients inserted
     */
    int bulkInsertRecipients(Integer surveyId, Integer departmentId);

    int bulkInsertCustomRecipients(Integer surveyId, List<Integer> studentIds);

    void syncCustomRecipients(Integer surveyId, List<Integer> studentIds);
}
