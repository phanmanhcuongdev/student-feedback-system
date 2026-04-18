package com.ttcs.backend.application.port.out.admin;

public interface LoadPendingStudentsPort {
    PendingStudentSearchPage loadPage(ManagePendingStudentsQuery query);
}
