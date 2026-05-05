package com.ttcs.backend.application.port.out;

import java.time.LocalDateTime;
import java.util.List;

public interface LoadSurveyReminderCandidatePort {
    List<SurveyReminderCandidate> loadUnsubmittedSurveysClosingBetween(LocalDateTime now, LocalDateTime deadlineTo);
}
