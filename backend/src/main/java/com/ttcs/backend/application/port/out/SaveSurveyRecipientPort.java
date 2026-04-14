package com.ttcs.backend.application.port.out;

import com.ttcs.backend.application.domain.model.SurveyRecipient;

import java.util.List;

public interface SaveSurveyRecipientPort {
    SurveyRecipient save(SurveyRecipient recipient);

    List<SurveyRecipient> saveAll(List<SurveyRecipient> recipients);
}
