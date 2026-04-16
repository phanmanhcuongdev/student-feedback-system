package com.ttcs.backend.application.port.in.auth;

import com.ttcs.backend.application.port.in.auth.result.StudentOnboardingStatusResult;

public interface GetStudentOnboardingStatusUseCase {
    StudentOnboardingStatusResult getStatus(Integer studentId);
}
