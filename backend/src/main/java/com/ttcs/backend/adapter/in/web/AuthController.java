package com.ttcs.backend.adapter.in.web;

import com.ttcs.backend.adapter.in.web.dto.LoginRequest;
import com.ttcs.backend.adapter.in.web.dto.LoginResponse;
import com.ttcs.backend.adapter.in.web.dto.RegisterStudentRequest;
import com.ttcs.backend.adapter.in.web.dto.RegisterStudentResponse;
import com.ttcs.backend.adapter.in.web.dto.UploadDocumentsResponse;
import com.ttcs.backend.adapter.in.web.dto.VerifyEmailResponse;
import com.ttcs.backend.application.port.in.auth.LoginUseCase;
import com.ttcs.backend.application.port.in.auth.RegisterStudentUseCase;
import com.ttcs.backend.application.port.in.auth.UploadStudentDocumentsUseCase;
import com.ttcs.backend.application.port.in.auth.VerifyEmailUseCase;
import com.ttcs.backend.application.port.in.auth.command.LoginCommand;
import com.ttcs.backend.application.port.in.auth.command.RegisterStudentCommand;
import com.ttcs.backend.application.port.in.auth.command.UploadStudentDocumentsCommand;
import com.ttcs.backend.application.port.in.auth.command.VerifyEmailCommand;
import com.ttcs.backend.application.port.in.auth.result.LoginResult;
import com.ttcs.backend.application.port.in.auth.result.RegisterStudentResult;
import com.ttcs.backend.application.port.in.auth.result.UploadStudentDocumentsResult;
import com.ttcs.backend.application.port.in.auth.result.VerifyEmailResult;
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

    private final RegisterStudentUseCase registerStudentUseCase;
    private final VerifyEmailUseCase verifyEmailUseCase;
    private final UploadStudentDocumentsUseCase uploadStudentDocumentsUseCase;
    private final LoginUseCase loginUseCase;

    @PostMapping("/register-student")
    public ResponseEntity<RegisterStudentResponse> registerStudent(@Valid @RequestBody RegisterStudentRequest request) {
        RegisterStudentResult result = registerStudentUseCase.register(
                new RegisterStudentCommand(
                        request.getName(),
                        request.getEmail(),
                        request.getPassword(),
                        request.getStudentCode(),
                        request.getDepartmentName()
                )
        );

        return ResponseEntity.ok(new RegisterStudentResponse(
                result.success(),
                result.code(),
                result.message(),
                result.verificationUrl()
        ));
    }

    @GetMapping("/verify-email")
    public ResponseEntity<VerifyEmailResponse> verifyEmail(@RequestParam("token") String token) {
        VerifyEmailResult result = verifyEmailUseCase.verify(new VerifyEmailCommand(token));

        return ResponseEntity.ok(new VerifyEmailResponse(
                result.success(),
                result.code(),
                result.message(),
                result.studentId(),
                result.studentStatus()
        ));
    }

    @PostMapping("/upload-docs")
    public ResponseEntity<UploadDocumentsResponse> uploadDocs(
            @RequestParam("studentId") Integer studentId,
            @RequestPart("studentCard") MultipartFile studentCard,
            @RequestPart("nationalId") MultipartFile nationalId
    ) {
        UploadStudentDocumentsResult result = uploadStudentDocumentsUseCase.upload(
                new UploadStudentDocumentsCommand(studentId, studentCard, nationalId)
        );

        return ResponseEntity.ok(new UploadDocumentsResponse(
                result.isSuccess(),
                result.getCode(),
                result.getMessage()
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResult result = loginUseCase.login(new LoginCommand(request.getEmail(), request.getPassword()));

        if (result.success()) {
            return ResponseEntity.ok(LoginResponse.success(
                    result.userId(),
                    result.role(),
                    result.studentStatus(),
                    result.accessToken()
            ));
        }

        return ResponseEntity.ok(LoginResponse.fail(result.code(), result.message()));
    }
}
