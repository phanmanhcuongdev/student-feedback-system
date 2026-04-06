package com.ttcs.backend.adapter.out.persistence;

import com.ttcs.backend.application.port.out.auth.StoreStudentDocumentPort;
import com.ttcs.backend.common.PersistenceAdapter;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@PersistenceAdapter
public class LocalStudentDocumentStorageAdapter implements StoreStudentDocumentPort {

    private static final String ROOT_DIR = "uploads/student-docs";

    @Override
    public String save(MultipartFile file, String prefix) {
        try {
            Path root = Paths.get(ROOT_DIR);
            if (!Files.exists(root)) {
                Files.createDirectories(root);
            }

            String original = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
            String safeName = UUID.randomUUID() + "-" + original;
            Path target = root.resolve(prefix + "-" + safeName);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            return target.toString().replace("\\", "/");
        } catch (IOException e) {
            throw new RuntimeException("Khong the luu file: " + e.getMessage(), e);
        }
    }
}
