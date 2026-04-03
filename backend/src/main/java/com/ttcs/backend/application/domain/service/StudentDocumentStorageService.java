package com.ttcs.backend.application.domain.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class StudentDocumentStorageService {

    private static final String ROOT_DIR = "uploads/student-docs";

    public String save(MultipartFile file, String prefix) {
        try {
            Path root = Paths.get(ROOT_DIR);
            if (!Files.exists(root)) {
                Files.createDirectories(root);
            }

            String safeName = UUID.randomUUID() + "-" + file.getOriginalFilename();
            Path target = root.resolve(prefix + "-" + safeName);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            return target.toString().replace("\\", "/");
        } catch (IOException e) {
            throw new RuntimeException("Khong the luu file: " + e.getMessage(), e);
        }
    }
}
