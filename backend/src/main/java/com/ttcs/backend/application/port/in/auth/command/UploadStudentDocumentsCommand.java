package com.ttcs.backend.application.port.in.auth.command;

import org.springframework.web.multipart.MultipartFile;

public record UploadStudentDocumentsCommand(
        Integer studentId,
        MultipartFile studentCard,
        MultipartFile nationalId
) {
}
