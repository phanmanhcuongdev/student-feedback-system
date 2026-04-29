package com.ttcs.backend.adapter.out.persistence.surveyresult;

public record RecipientSummary(long targetedCount, long openedCount, long submittedCount, double responseRate) {
}
