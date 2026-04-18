package com.ttcs.backend.application.port.in.admin;

public interface GetPendingStudentsUseCase {
    PendingStudentPageResult getPendingStudents(GetPendingStudentsQuery query);
}
