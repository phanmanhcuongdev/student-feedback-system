package com.ttcs.backend.application.port.in.auth;

import com.ttcs.backend.application.port.in.auth.command.UploadStudentDocumentsCommand;
import com.ttcs.backend.application.port.in.auth.result.UploadStudentDocumentsResult;

public interface UploadStudentDocumentsUseCase {
    UploadStudentDocumentsResult upload(UploadStudentDocumentsCommand command);
}
