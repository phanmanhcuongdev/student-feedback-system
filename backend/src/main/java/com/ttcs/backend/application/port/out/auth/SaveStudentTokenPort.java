package com.ttcs.backend.application.port.out.auth;

import com.ttcs.backend.application.domain.model.StudentToken;

public interface SaveStudentTokenPort {
    StudentToken save(StudentToken token);
}
