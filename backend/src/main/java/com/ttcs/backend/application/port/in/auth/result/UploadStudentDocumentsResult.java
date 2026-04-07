package com.ttcs.backend.application.port.in.auth.result;

import lombok.Getter;

@Getter
public class UploadStudentDocumentsResult {
    private final boolean success;
    private final String code;
    private final String message;

    public UploadStudentDocumentsResult(boolean success, String code, String message) {
        this.success = success;
        this.code = code;
        this.message = message;
    }

    public static UploadStudentDocumentsResult ok() {
        return new UploadStudentDocumentsResult(true, "UPLOAD_DOCS_SUCCESS", "Gửi minh chứng thành công.");
    }

    public static UploadStudentDocumentsResult fail(String code, String message) {
        return new UploadStudentDocumentsResult(false, code, message);
    }

}
