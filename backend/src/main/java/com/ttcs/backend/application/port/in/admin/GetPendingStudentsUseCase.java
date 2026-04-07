package com.ttcs.backend.application.port.in.admin;

import java.util.List;

public interface GetPendingStudentsUseCase {
    List<PendingStudentResult> getPendingStudents();
}
