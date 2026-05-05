package com.ttcs.backend.adapter.out.persistence.surveyrecipient;

import com.ttcs.backend.application.port.out.LoadSurveyReminderCandidatePort;
import com.ttcs.backend.application.port.out.SurveyReminderCandidate;
import com.ttcs.backend.common.PersistenceAdapter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@PersistenceAdapter
@RequiredArgsConstructor
public class SurveyReminderPersistenceAdapter implements LoadSurveyReminderCandidatePort {

    private final SurveyRecipientRepository surveyRecipientRepository;

    @Override
    public List<SurveyReminderCandidate> loadUnsubmittedSurveysClosingBetween(LocalDateTime now, LocalDateTime deadlineTo) {
        if (now == null || deadlineTo == null || deadlineTo.isBefore(now)) {
            return List.of();
        }
        return surveyRecipientRepository.findUnsubmittedClosingBetween(now, deadlineTo).stream()
                .map(recipient -> new SurveyReminderCandidate(
                        recipient.getStudent().getId(),
                        recipient.getSurvey().getId(),
                        recipient.getSurvey().getTitle(),
                        recipient.getSurvey().getEndDate()
                ))
                .toList();
    }
}
