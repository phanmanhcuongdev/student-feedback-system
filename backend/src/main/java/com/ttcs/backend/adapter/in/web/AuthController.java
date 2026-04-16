package com.ttcs.backend.adapter.in.web;

import com.ttcs.backend.adapter.in.web.dto.ChangePasswordRequest;
import com.ttcs.backend.adapter.in.web.dto.ChangePasswordResponse;
import com.ttcs.backend.adapter.in.web.dto.ForgotPasswordRequest;
import com.ttcs.backend.adapter.in.web.dto.ForgotPasswordResponse;
import com.ttcs.backend.adapter.in.web.dto.LoginRequest;
import com.ttcs.backend.adapter.in.web.dto.LoginResponse;
import com.ttcs.backend.adapter.in.web.dto.RegisterStudentRequest;
import com.ttcs.backend.adapter.in.web.dto.RegisterStudentResponse;
import com.ttcs.backend.adapter.in.web.dto.ResetPasswordRequest;
import com.ttcs.backend.adapter.in.web.dto.ResetPasswordResponse;
import com.ttcs.backend.adapter.in.web.dto.StudentOnboardingStatusResponse;
import com.ttcs.backend.adapter.in.web.dto.UploadDocumentsResponse;
import com.ttcs.backend.adapter.in.web.dto.VerifyEmailResponse;
import com.ttcs.backend.application.port.in.auth.ChangePasswordUseCase;
import com.ttcs.backend.application.port.in.auth.ForgotPasswordUseCase;
import com.ttcs.backend.application.port.in.auth.GetStudentOnboardingStatusUseCase;
import com.ttcs.backend.application.port.in.auth.LoginUseCase;
import com.ttcs.backend.application.port.in.auth.RegisterStudentUseCase;
import com.ttcs.backend.application.port.in.auth.ResetPasswordUseCase;
import com.ttcs.backend.application.port.in.auth.UploadStudentDocumentsUseCase;
import com.ttcs.backend.application.port.in.auth.VerifyEmailUseCase;
import com.ttcs.backend.application.port.in.auth.command.ChangePasswordCommand;
import com.ttcs.backend.application.port.in.auth.command.ForgotPasswordCommand;
import com.ttcs.backend.application.port.in.auth.command.LoginCommand;
import com.ttcs.backend.application.port.in.auth.command.RegisterStudentCommand;
import com.ttcs.backend.application.port.in.auth.command.ResetPasswordCommand;
import com.ttcs.backend.application.port.in.auth.command.UploadStudentDocumentsCommand;
import com.ttcs.backend.application.port.in.auth.command.VerifyEmailCommand;
import com.ttcs.backend.application.port.in.auth.result.ChangePasswordResult;
import com.ttcs.backend.application.port.in.auth.result.ForgotPasswordResult;
import com.ttcs.backend.application.port.in.auth.result.LoginResult;
import com.ttcs.backend.application.port.in.auth.result.RegisterStudentResult;
import com.ttcs.backend.application.port.in.auth.result.ResetPasswordResult;
import com.ttcs.backend.application.port.in.auth.result.StudentOnboardingStatusResult;
import com.ttcs.backend.application.port.in.auth.result.UploadStudentDocumentsResult;
import com.ttcs.backend.application.port.in.auth.result.VerifyEmailResult;
import com.ttcs.backend.common.WebAdapter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@WebAdapter
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RegisterStudentUseCase registerStudentUseCase;
    private final VerifyEmailUseCase verifyEmailUseCase;
    private final UploadStudentDocumentsUseCase uploadStudentDocumentsUseCase;
    private final GetStudentOnboardingStatusUseCase getStudentOnboardingStatusUseCase;
    private final LoginUseCase loginUseCase;
    private final ChangePasswordUseCase changePasswordUseCase;
    private final ForgotPasswordUseCase forgotPasswordUseCase;
    private final ResetPasswordUseCase resetPasswordUseCase;
    private final CurrentStudentProvider currentStudentProvider;

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
                result.message()
        ));
    }

    @GetMapping("/verify-email")
    public ResponseEntity<VerifyEmailResponse> verifyEmail(@RequestParam("token") String token) {
        VerifyEmailResult result = verifyEmailUseCase.verify(new VerifyEmailCommand(token));

        return ResponseEntity.ok(new VerifyEmailResponse(
                result.success(),
                result.code(),
                result.message(),
                result.studentStatus()
        ));
    }

    @PostMapping("/upload-docs")
    public ResponseEntity<UploadDocumentsResponse> uploadDocs(
            @RequestPart("studentCard") MultipartFile studentCard,
            @RequestPart("nationalId") MultipartFile nationalId
    ) {
        UploadStudentDocumentsResult result = uploadStudentDocumentsUseCase.upload(
                new UploadStudentDocumentsCommand(currentStudentProvider.currentStudentId(), studentCard, nationalId)
        );

        return ResponseEntity.ok(new UploadDocumentsResponse(
                result.isSuccess(),
                result.getCode(),
                result.getMessage()
        ));
    }

    @GetMapping("/onboarding-status")
    public ResponseEntity<StudentOnboardingStatusResponse> getOnboardingStatus() {
        StudentOnboardingStatusResult result = getStudentOnboardingStatusUseCase.getStatus(
                currentStudentProvider.currentStudentId()
        );

        return ResponseEntity.ok(new StudentOnboardingStatusResponse(
                result.success(),
                result.code(),
                result.message(),
                result.status(),
                result.reviewReason(),
                result.reviewNotes(),
                result.hasUploadedDocuments(),
                result.canUploadDocuments(),
                result.resubmissionCount()
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

    @PostMapping("/change-password")
    public ResponseEntity<ChangePasswordResponse> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        ChangePasswordResult result = changePasswordUseCase.changePassword(
                new ChangePasswordCommand(
                        currentStudentProvider.currentUserId(),
                        request.getCurrentPassword(),
                        request.getNewPassword()
                )
        );
        return ResponseEntity.ok(new ChangePasswordResponse(result.success(), result.code(), result.message()));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ForgotPasswordResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        ForgotPasswordResult result = forgotPasswordUseCase.forgotPassword(
                new ForgotPasswordCommand(request.getEmail())
        );
        return ResponseEntity.ok(new ForgotPasswordResponse(result.success(), result.code(), result.message()));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ResetPasswordResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        ResetPasswordResult result = resetPasswordUseCase.resetPassword(
                new ResetPasswordCommand(request.getToken(), request.getNewPassword())
        );
        return ResponseEntity.ok(new ResetPasswordResponse(result.success(), result.code(), result.message()));
    }
}
