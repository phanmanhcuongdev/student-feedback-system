package com.ttcs.backend.application.domain.service;

import com.ttcs.backend.application.domain.model.Department;
import com.ttcs.backend.application.domain.model.PasswordResetToken;
import com.ttcs.backend.application.domain.model.Role;
import com.ttcs.backend.application.domain.model.Status;
import com.ttcs.backend.application.domain.model.Student;
import com.ttcs.backend.application.domain.model.StudentToken;
import com.ttcs.backend.application.domain.model.User;
import com.ttcs.backend.application.domain.exception.VerifyEmailDeliveryException;
import com.ttcs.backend.application.port.in.auth.ChangePasswordUseCase;
import com.ttcs.backend.application.port.in.auth.ForgotPasswordUseCase;
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
import com.ttcs.backend.application.port.in.auth.result.UploadStudentDocumentsResult;
import com.ttcs.backend.application.port.in.auth.result.VerifyEmailResult;
import com.ttcs.backend.application.port.out.auth.*;
import com.ttcs.backend.common.UseCase;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.UUID;

@UseCase
@RequiredArgsConstructor
public class AuthUseCaseService implements
        RegisterStudentUseCase,
        LoginUseCase,
        VerifyEmailUseCase,
        UploadStudentDocumentsUseCase,
        ChangePasswordUseCase,
        ForgotPasswordUseCase,
        ResetPasswordUseCase {

    private static final Logger log = LoggerFactory.getLogger(AuthUseCaseService.class);

    private final LoadUserByEmailPort loadUserByEmailPort;
    private final LoadUserByIdPort loadUserByIdPort;
    private final SaveUserPort saveUserPort;

    private final LoadStudentByIdPort loadStudentByIdPort;
    private final SaveStudentPort saveStudentPort;

    private final LoadStudentTokenPort loadStudentTokenPort;
    private final SaveStudentTokenPort saveStudentTokenPort;
    private final LoadPasswordResetTokenPort loadPasswordResetTokenPort;
    private final SavePasswordResetTokenPort savePasswordResetTokenPort;

    private final LoadDepartmentPort loadDepartmentPort;
    private final SendVerifyEmailPort sendVerifyEmailPort;
    private final SendPasswordResetEmailPort sendPasswordResetEmailPort;
    private final StoreStudentDocumentPort storeStudentDocumentPort;

    private final PasswordEncoder passwordEncoder;

    private final JwtTokenPort jwtTokenPort;

    @Value("${app.verify.email-url-base:http://localhost:5173}")
    private String verifyEmailUrlBase;

    @Value("${app.reset-password.url-base:http://localhost:5173}")
    private String resetPasswordUrlBase;

    @Value("${app.reset-password.expiration-minutes:30}")
    private long resetPasswordExpirationMinutes;

    @Override
    @Transactional
    public RegisterStudentResult register(RegisterStudentCommand command) {
        if (command == null
                || isBlank(command.email())
                || isBlank(command.password())
                || isBlank(command.name())
                || isBlank(command.studentCode())
                || isBlank(command.departmentName())) {
            return RegisterStudentResult.fail("INVALID_INPUT", "Thong tin dang ky khong hop le");
        }

        if (saveUserPort.existsByEmail(command.email())) {
            return RegisterStudentResult.fail("EMAIL_ALREADY_USED", "Email da duoc su dung");
        }

        if (saveStudentPort.existsByStudentCode(command.studentCode())) {
            return RegisterStudentResult.fail("STUDENT_CODE_ALREADY_USED", "Ma sinh vien da duoc su dung");
        }

        Department department = loadDepartmentPort.loadByName(command.departmentName()).orElse(null);
        if (department == null) {
            return RegisterStudentResult.fail("DEPARTMENT_NOT_FOUND", "Khoa khong ton tai");
        }

        User userToSave = new User(
                null,
                command.email(),
                passwordEncoder.encode(command.password()),
                Role.STUDENT,
                false
        );
        User savedUser = saveUserPort.save(userToSave);

        Student studentToSave = new Student(
                savedUser.getId(),
                null,
                command.name(),
                command.studentCode(),
                department,
                Status.EMAIL_UNVERIFIED,
                null,
                null
        );
        Student savedStudent = saveStudentPort.save(studentToSave);

        StudentToken token = new StudentToken(
                null,
                savedStudent,
                UUID.randomUUID().toString(),
                LocalDateTime.now().plusHours(24),
                LocalDateTime.now(),
                0
        );
        StudentToken savedToken = saveStudentTokenPort.save(token);

        String verifyUrl = verifyEmailUrlBase + "/verify-email?token=" + savedToken.getToken();
        try {
            sendVerifyEmailPort.sendVerifyEmail(savedUser.getEmail(), verifyUrl);
        } catch (RuntimeException ex) {
            throw new VerifyEmailDeliveryException(
                    "Khong the gui email xac minh luc nay. Vui long thu lai sau.",
                    ex
            );
        }

        return RegisterStudentResult.ok();
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResult login(LoginCommand command) {
        if (command == null || isBlank(command.email()) || isBlank(command.password())) {
            return LoginResult.fail("INVALID_INPUT", "Thong tin dang nhap khong hop le");
        }

        User user = loadUserByEmailPort.loadByEmail(command.email()).orElse(null);
        if (user == null || !passwordEncoder.matches(command.password(), user.getPassword())) {
            return LoginResult.fail("INVALID_CREDENTIALS", "Sai email hoac mat khau");
        }
        if (!Boolean.TRUE.equals(user.getVerified())) {
            return LoginResult.fail("ACCOUNT_INACTIVE", "Tai khoan da bi vo hieu hoa hoac chua duoc kich hoat.");
        }

        String studentStatus = null;
        if (user.getRole() == Role.STUDENT) {
            Student student = loadStudentByIdPort.loadByUserId(user.getId()).orElse(null);
            if (student == null) {
                return LoginResult.fail("STUDENT_PROFILE_NOT_FOUND", "Khong tim thay thong tin sinh vien");
            }
            studentStatus = student.getStatus().name();
            if (student.getStatus() != Status.ACTIVE && student.getStatus() != Status.EMAIL_VERIFIED) {
                return blockedStudentLoginResult(student);
            }
        }

        String accessToken = jwtTokenPort.generateAccessToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name()
        );

        return LoginResult.ok(user.getId(), user.getRole().name(), studentStatus, accessToken);
    }


    @Override
    @Transactional
    public VerifyEmailResult verify(VerifyEmailCommand command) {
        if (command == null || isBlank(command.token())) {
            return VerifyEmailResult.fail("INVALID_INPUT", "Token khong hop le");
        }

        StudentToken token = loadStudentTokenPort
                .loadByTokenAndDeleteFlg(command.token(), 0)
                .orElse(null);

        if (token == null) {
            return VerifyEmailResult.fail("TOKEN_INVALID", "Token khong hop le hoac da duoc su dung");
        }

        if (LocalDateTime.now().isAfter(token.getExpiredAt())) {
            return VerifyEmailResult.fail("TOKEN_EXPIRED", "Token da het hieu luc");
        }

        Student student = token.getStudent();
        if (student == null) {
            return VerifyEmailResult.fail("STUDENT_NOT_FOUND", "Khong tim thay sinh vien");
        }

        if (student.getStatus() == Status.EMAIL_UNVERIFIED) {
            Student updatedStudent = new Student(
                    student.getId(),
                    student.getUser(),
                    student.getName(),
                    student.getStudentCode(),
                    student.getDepartment(),
                    Status.EMAIL_VERIFIED,
                    student.getStudentCardImageUrl(),
                    student.getNationalIdImageUrl()
            );
            saveStudentPort.save(updatedStudent);
            if (student.getUser() != null) {
                User updatedUser = new User(
                        student.getUser().getId(),
                        student.getUser().getEmail(),
                        student.getUser().getPassword(),
                        student.getUser().getRole(),
                        true
                );
                saveUserPort.save(updatedUser);
            }
            student = updatedStudent;
        } else if (student.getStatus() == Status.ACTIVE
                || student.getStatus() == Status.EMAIL_VERIFIED
                || student.getStatus() == Status.PENDING
                || student.getStatus() == Status.REJECTED) {
            StudentToken usedToken = new StudentToken(
                    token.getId(),
                    token.getStudent(),
                    token.getToken(),
                    token.getExpiredAt(),
                    token.getCreatedAt(),
                    1
            );
            saveStudentTokenPort.save(usedToken);
            return VerifyEmailResult.fail("ALREADY_VERIFIED", "Email nay da duoc xac minh truoc do.");
        }

        StudentToken usedToken = new StudentToken(
                token.getId(),
                token.getStudent(),
                token.getToken(),
                token.getExpiredAt(),
                token.getCreatedAt(),
                1
        );
        saveStudentTokenPort.save(usedToken);

        return VerifyEmailResult.ok(student.getStatus().name());
    }

    @Override
    @Transactional
    public UploadStudentDocumentsResult upload(UploadStudentDocumentsCommand command) {
        if (command == null
                || command.studentCard() == null
                || command.nationalId() == null) {
            return UploadStudentDocumentsResult.fail("INVALID_INPUT", "Thong tin upload khong hop le");
        }

        Student student = loadStudentByIdPort.loadById(command.studentId()).orElse(null);
        if (student == null) {
            return UploadStudentDocumentsResult.fail("STUDENT_NOT_FOUND", "Khong tim thay sinh vien");
        }

        if (student.getStatus() == Status.EMAIL_UNVERIFIED) {
            return UploadStudentDocumentsResult.fail(
                    "EMAIL_NOT_VERIFIED",
                    "Ban can xac minh email truoc khi tai len minh chung."
            );
        }
        if (student.getStatus() != Status.EMAIL_VERIFIED) {
            return UploadStudentDocumentsResult.fail(
                "INVALID_STATUS",
                "Tai khoan khong o trang thai cho phep tai len minh chung."
            );
        }
        if (hasUploadedDocuments(student)) {
            return UploadStudentDocumentsResult.fail(
                    "INVALID_STATUS",
                    "Tai khoan da gui minh chung va dang cho phe duyet."
            );
        }

        String cardPath = storeStudentDocumentPort.save(command.studentCard(), "student-card");
        String nationalIdPath = storeStudentDocumentPort.save(command.nationalId(), "national-id");

        Student updatedStudent = new Student(
                student.getId(),
                student.getUser(),
                student.getName(),
                student.getStudentCode(),
                student.getDepartment(),
                Status.PENDING,
                cardPath,
                nationalIdPath
        );
        saveStudentPort.save(updatedStudent);

        return UploadStudentDocumentsResult.ok();
    }

    @Override
    @Transactional
    public ForgotPasswordResult forgotPassword(ForgotPasswordCommand command) {
        if (command == null || isBlank(command.email())) {
            return ForgotPasswordResult.fail("INVALID_INPUT", "Email is required.");
        }

        User user = loadUserByEmailPort.loadByEmail(command.email().trim()).orElse(null);
        if (user == null) {
            return ForgotPasswordResult.ok();
        }

        savePasswordResetTokenPort.markActiveTokensUsed(user.getId());

        String rawToken = generateRawToken();
        String tokenHash = hashToken(rawToken);
        PasswordResetToken resetToken = new PasswordResetToken(
                null,
                user,
                tokenHash,
                LocalDateTime.now().plusMinutes(resetPasswordExpirationMinutes),
                LocalDateTime.now(),
                null
        );
        savePasswordResetTokenPort.save(resetToken);

        String resetUrl = resetPasswordUrlBase + "/reset-password?token=" + rawToken;
        try {
            sendPasswordResetEmailPort.sendPasswordResetEmail(user.getEmail(), resetUrl);
        } catch (RuntimeException ex) {
            log.warn("Failed to send password reset email for userId={}", user.getId(), ex);
        }

        return ForgotPasswordResult.ok();
    }

    @Override
    @Transactional
    public ChangePasswordResult changePassword(ChangePasswordCommand command) {
        if (command == null
                || command.userId() == null
                || isBlank(command.currentPassword())
                || isBlank(command.newPassword())) {
            return ChangePasswordResult.fail("INVALID_INPUT", "Current password and new password are required.");
        }

        User user = loadUserByIdPort.loadById(command.userId()).orElse(null);
        if (user == null) {
            return ChangePasswordResult.fail("USER_NOT_FOUND", "Authenticated user was not found.");
        }

        if (!passwordEncoder.matches(command.currentPassword(), user.getPassword())) {
            return ChangePasswordResult.fail("CURRENT_PASSWORD_INCORRECT", "Current password is incorrect.");
        }

        if (passwordEncoder.matches(command.newPassword(), user.getPassword())) {
            return ChangePasswordResult.fail("PASSWORD_UNCHANGED", "New password must be different from the current password.");
        }

        User updatedUser = new User(
                user.getId(),
                user.getEmail(),
                passwordEncoder.encode(command.newPassword()),
                user.getRole(),
                user.getVerified()
        );
        saveUserPort.save(updatedUser);

        return ChangePasswordResult.ok();
    }

    @Override
    @Transactional
    public ResetPasswordResult resetPassword(ResetPasswordCommand command) {
        if (command == null || isBlank(command.token()) || isBlank(command.newPassword())) {
            return ResetPasswordResult.fail("INVALID_INPUT", "Token and new password are required.");
        }

        PasswordResetToken token = loadPasswordResetTokenPort.loadActiveByTokenHash(hashToken(command.token()))
                .orElse(null);
        if (token == null || token.getUsedAt() != null) {
            return ResetPasswordResult.fail("TOKEN_INVALID", "Reset token is invalid or has already been used.");
        }

        if (LocalDateTime.now().isAfter(token.getExpiredAt())) {
            return ResetPasswordResult.fail("TOKEN_EXPIRED", "Reset token has expired.");
        }

        User user = token.getUser();
        if (user == null) {
            return ResetPasswordResult.fail("TOKEN_INVALID", "Reset token is invalid or has already been used.");
        }

        User updatedUser = new User(
                user.getId(),
                user.getEmail(),
                passwordEncoder.encode(command.newPassword()),
                user.getRole(),
                user.getVerified()
        );
        saveUserPort.save(updatedUser);
        savePasswordResetTokenPort.markActiveTokensUsed(user.getId());

        return ResetPasswordResult.ok();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private boolean hasUploadedDocuments(Student student) {
        return !isBlank(student.getStudentCardImageUrl()) && !isBlank(student.getNationalIdImageUrl());
    }

    private String generateRawToken() {
        return UUID.randomUUID() + "-" + UUID.randomUUID();
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(token.trim().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to hash token", ex);
        }
    }

    private LoginResult blockedStudentLoginResult(Student student) {
        return switch (student.getStatus()) {
            case PENDING -> LoginResult.fail(
                    "ACCOUNT_PENDING",
                    "Tai khoan dang cho quan tri vien phe duyet."
            );
            case REJECTED -> LoginResult.fail(
                    "ACCOUNT_REJECTED",
                    "Tai khoan da bi tu choi. Vui long lien he quan tri vien."
            );
            case EMAIL_VERIFIED, ACTIVE -> throw new IllegalStateException(
                    "Verified or active student should not be blocked from login"
            );
            case EMAIL_UNVERIFIED -> LoginResult.fail(
                    "ACCOUNT_INACTIVE",
                    "Tai khoan chua duoc kich hoat. Vui long xac minh email va hoan tat quy trinh xac thuc."
            );
        };
    }
}
