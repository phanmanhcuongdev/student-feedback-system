package com.ttcs.backend.adapter.out.persistence;

import com.ttcs.backend.application.port.out.auth.StoreStudentDocumentPort;
import com.ttcs.backend.application.port.out.auth.StudentDocumentContent;
import com.ttcs.backend.common.PersistenceAdapter;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.MinioException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.Optional;
import java.util.UUID;

@PersistenceAdapter
@RequiredArgsConstructor
public class MinioStudentDocumentStorageAdapter implements StoreStudentDocumentPort {

    private final MinioClient minioClient;

    @Value("${app.storage.minio.bucket:student-documents}")
    private String defaultBucket;

    @Override
    public String save(MultipartFile file, String prefix) {
        ensureBucketExists(defaultBucket);

        String originalFilename = sanitizeFilename(file.getOriginalFilename());
        String objectName = prefix + "/" + UUID.randomUUID() + "-" + originalFilename;
        String contentType = Optional.ofNullable(file.getContentType())
                .filter(value -> !value.isBlank())
                .orElse(MediaType.APPLICATION_OCTET_STREAM_VALUE);

        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(defaultBucket)
                            .object(objectName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(contentType)
                            .build()
            );
            return defaultBucket + "/" + objectName;
        } catch (IOException | GeneralSecurityException | MinioException ex) {
            throw new RuntimeException("Khong the luu file len MinIO.", ex);
        }
    }

    @Override
    public StudentDocumentContent load(String location) {
        StoredLocation storedLocation = parseLocation(location);

        try (InputStream inputStream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(storedLocation.bucket())
                        .object(storedLocation.object())
                        .build()
        )) {
            StatObjectResponse stat = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(storedLocation.bucket())
                            .object(storedLocation.object())
                            .build()
            );

            String contentType = Optional.ofNullable(stat.contentType())
                    .filter(value -> !value.isBlank())
                    .orElse(MediaType.APPLICATION_OCTET_STREAM_VALUE);

            return new StudentDocumentContent(
                    extractFilename(storedLocation.object()),
                    contentType,
                    inputStream.readAllBytes()
            );
        } catch (ErrorResponseException ex) {
            if (ex.errorResponse() != null && "NoSuchKey".equalsIgnoreCase(ex.errorResponse().code())) {
                throw new IllegalArgumentException("DOCUMENT_NOT_FOUND");
            }
            throw new RuntimeException("Khong the tai file tu MinIO.", ex);
        } catch (IOException | GeneralSecurityException | MinioException ex) {
            throw new RuntimeException("Khong the tai file tu MinIO.", ex);
        }
    }

    private void ensureBucketExists(String bucket) {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(bucket)
                            .build()
            );
            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(bucket)
                                .build()
                );
            }
        } catch (MinioException | IOException | GeneralSecurityException ex) {
            throw new RuntimeException("Khong the khoi tao bucket MinIO.", ex);
        }
    }

    private StoredLocation parseLocation(String location) {
        if (location == null || location.trim().isEmpty()) {
            throw new IllegalArgumentException("DOCUMENT_NOT_FOUND");
        }

        String normalized = location.trim().replace("\\", "/");

        if (normalized.contains("://")) {
            URI uri = URI.create(normalized);
            normalized = Optional.ofNullable(uri.getPath()).orElse("").replaceFirst("^/+", "");
        } else {
            normalized = normalized.replaceFirst("^/+", "");
        }

        if (normalized.startsWith(defaultBucket + "/")) {
            return new StoredLocation(defaultBucket, normalized.substring(defaultBucket.length() + 1));
        }

        int slashIndex = normalized.indexOf('/');
        if (slashIndex > 0) {
            return new StoredLocation(normalized.substring(0, slashIndex), normalized.substring(slashIndex + 1));
        }

        return new StoredLocation(defaultBucket, normalized);
    }

    private String sanitizeFilename(String originalFilename) {
        String filename = Optional.ofNullable(originalFilename)
                .map(value -> value.replace("\\", "/"))
                .map(value -> Paths.get(value).getFileName().toString())
                .filter(value -> !value.isBlank())
                .orElse("file");

        return filename.replaceAll("[^A-Za-z0-9._-]", "_");
    }

    private String extractFilename(String objectName) {
        int slashIndex = objectName.lastIndexOf('/');
        return slashIndex >= 0 ? objectName.substring(slashIndex + 1) : objectName;
    }

    private record StoredLocation(String bucket, String object) {
    }
}
