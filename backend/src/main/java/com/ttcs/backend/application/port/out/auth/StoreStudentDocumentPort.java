package com.ttcs.backend.application.port.out.auth;

import org.springframework.web.multipart.MultipartFile;

public interface StoreStudentDocumentPort {
    String save(MultipartFile file, String prefix);
}
