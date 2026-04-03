package com.ttcs.backend.adapter.in.web;

import com.ttcs.backend.adapter.in.web.dto.LoginRequest;
import com.ttcs.backend.adapter.in.web.dto.LoginResponse;
import com.ttcs.backend.adapter.in.web.dto.RegisterStudentRequest;
import com.ttcs.backend.adapter.in.web.dto.RegisterStudentResponse;
import com.ttcs.backend.adapter.in.web.dto.UploadDocumentsResponse;
import com.ttcs.backend.adapter.in.web.dto.VerifyEmailResponse;
import com.ttcs.backend.application.domain.service.AuthService;
import com.ttcs.backend.common.WebAdapter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@WebAdapter
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register-student")
    public ResponseEntity<RegisterStudentResponse> registerStudent(@Valid @RequestBody RegisterStudentRequest request) {
        return ResponseEntity.ok(authService.registerStudent(request));
    }

    @GetMapping("/verify-email")
    public ResponseEntity<VerifyEmailResponse> verifyEmail(@RequestParam("token") String token) {
        return ResponseEntity.ok(authService.verifyEmail(token));
    }

    @PostMapping("/upload-docs")
    public ResponseEntity<UploadDocumentsResponse> uploadDocs(
            @RequestParam("studentId") Integer studentId,
            @RequestPart("studentCard") MultipartFile studentCard,
            @RequestPart("nationalId") MultipartFile nationalId
    ) {
        return ResponseEntity.ok(authService.uploadDocuments(studentId, studentCard, nationalId));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
