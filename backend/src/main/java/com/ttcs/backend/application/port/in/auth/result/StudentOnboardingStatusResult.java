package com.ttcs.backend.application.port.in.auth.result;

public record StudentOnboardingStatusResult(
        boolean success,
        String code,
        String message,
        String status,
        String reviewReason,
        String reviewNotes,
        boolean hasUploadedDocuments,
        boolean canUploadDocuments,
        int resubmissionCount
) {
    public static StudentOnboardingStatusResult ok(
            String status,
            String reviewReason,
            String reviewNotes,
            boolean hasUploadedDocuments,
            boolean canUploadDocuments,
            int resubmissionCount
    ) {
        return new StudentOnboardingStatusResult(
                true,
                "ONBOARDING_STATUS_SUCCESS",
                "Lay trang thai onboarding thanh cong",
                status,
                reviewReason,
                reviewNotes,
                hasUploadedDocuments,
                canUploadDocuments,
                resubmissionCount
        );
    }

    public static StudentOnboardingStatusResult fail(String code, String message) {
        return new StudentOnboardingStatusResult(false, code, message, null, null, null, false, false, 0);
    }
}
