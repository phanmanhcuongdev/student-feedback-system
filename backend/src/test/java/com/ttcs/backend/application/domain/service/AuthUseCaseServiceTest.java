package com.ttcs.backend.application.domain.service;

import com.ttcs.backend.application.domain.model.Department;
import com.ttcs.backend.application.domain.model.PasswordResetToken;
import com.ttcs.backend.application.domain.model.Role;
import com.ttcs.backend.application.domain.model.Status;
import com.ttcs.backend.application.domain.model.Student;
import com.ttcs.backend.application.domain.model.StudentToken;
import com.ttcs.backend.application.domain.model.User;
import com.ttcs.backend.application.port.in.auth.command.ForgotPasswordCommand;
import com.ttcs.backend.application.port.in.auth.command.LoginCommand;
import com.ttcs.backend.application.port.in.auth.command.ResetPasswordCommand;
import com.ttcs.backend.application.port.in.auth.command.ChangePasswordCommand;
import com.ttcs.backend.application.port.in.auth.command.UploadStudentDocumentsCommand;
import com.ttcs.backend.application.port.in.auth.result.ChangePasswordResult;
import com.ttcs.backend.application.port.in.auth.result.ForgotPasswordResult;
import com.ttcs.backend.application.port.in.auth.result.LoginResult;
import com.ttcs.backend.application.port.in.auth.result.ResetPasswordResult;
import com.ttcs.backend.application.port.in.auth.result.StudentOnboardingStatusResult;
import com.ttcs.backend.application.port.in.auth.result.UploadStudentDocumentsResult;
import com.ttcs.backend.application.port.out.auth.JwtTokenPort;
import com.ttcs.backend.application.port.out.auth.LoadDepartmentPort;
import com.ttcs.backend.application.port.out.auth.LoadPasswordResetTokenPort;
import com.ttcs.backend.application.port.out.auth.LoadStudentByIdPort;
import com.ttcs.backend.application.port.out.auth.LoadStudentTokenPort;
import com.ttcs.backend.application.port.out.auth.LoadUserByIdPort;
import com.ttcs.backend.application.port.out.auth.SavePasswordResetTokenPort;
import com.ttcs.backend.application.port.out.auth.SaveStudentPort;
import com.ttcs.backend.application.port.out.auth.SaveStudentTokenPort;
import com.ttcs.backend.application.port.out.auth.SaveUserPort;
import com.ttcs.backend.application.port.out.auth.SendPasswordResetEmailPort;
import com.ttcs.backend.application.port.out.auth.SendVerifyEmailPort;
import com.ttcs.backend.application.port.out.auth.StoreStudentDocumentPort;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthUseCaseServiceTest {

    @Test
    void shouldAllowActiveStudentToLogin() {
        AuthUseCaseService service = createFixture(student(Status.ACTIVE, true, true)).service;

        LoginResult result = service.login(new LoginCommand("student@example.com", "secret"));

        assertTrue(result.success());
        assertEquals("ACTIVE", result.studentStatus());
        assertEquals("jwt-token", result.accessToken());
    }

    @Test
    void shouldAllowEmailVerifiedStudentToLoginForDocumentUpload() {
        AuthUseCaseService service = createFixture(student(Status.EMAIL_VERIFIED, true, false)).service;

        LoginResult result = service.login(new LoginCommand("student@example.com", "secret"));

        assertTrue(result.success());
        assertEquals("EMAIL_VERIFIED", result.studentStatus());
        assertEquals("jwt-token", result.accessToken());
    }

    @Test
    void shouldRejectPendingStudentLogin() {
        AuthUseCaseService service = createFixture(student(Status.PENDING, true, true)).service;

        LoginResult result = service.login(new LoginCommand("student@example.com", "secret"));

        assertFalse(result.success());
        assertEquals("ACCOUNT_PENDING", result.code());
        assertNull(result.accessToken());
    }

    @Test
    void shouldAllowRejectedStudentLoginForResubmission() {
        AuthUseCaseService service = createFixture(student(Status.REJECTED, true, true)).service;

        LoginResult result = service.login(new LoginCommand("student@example.com", "secret"));

        assertTrue(result.success());
        assertEquals("REJECTED", result.studentStatus());
        assertEquals("jwt-token", result.accessToken());
    }

    @Test
    void shouldRejectInactiveStudentLogin() {
        AuthUseCaseService service = createFixture(student(Status.EMAIL_UNVERIFIED, false, false)).service;

        LoginResult result = service.login(new LoginCommand("student@example.com", "secret"));

        assertFalse(result.success());
        assertEquals("ACCOUNT_INACTIVE", result.code());
        assertNull(result.accessToken());
    }

    @Test
    void shouldRejectDeactivatedTeacherLogin() {
        AuthUseCaseService service = createFixtureWithUser(new User(2, "teacher@example.com", "secret", Role.TEACHER, false)).service;

        LoginResult result = service.login(new LoginCommand("teacher@example.com", "secret"));

        assertFalse(result.success());
        assertEquals("ACCOUNT_INACTIVE", result.code());
        assertNull(result.accessToken());
    }

    @Test
    void shouldReturnGenericSuccessWhenForgotPasswordEmailDoesNotExist() {
        AuthFixture fixture = createFixture(null);

        ForgotPasswordResult result = fixture.service.forgotPassword(new ForgotPasswordCommand("missing@example.com"));

        assertTrue(result.success());
        assertEquals("RESET_EMAIL_SENT", result.code());
        assertNull(fixture.passwordResetEmailPort.lastResetUrl);
    }

    @Test
    void shouldSendResetLinkForExistingUser() {
        AuthFixture fixture = createFixture(student(Status.ACTIVE, true, true));

        ForgotPasswordResult result = fixture.service.forgotPassword(new ForgotPasswordCommand("student@example.com"));

        assertTrue(result.success());
        assertEquals("RESET_EMAIL_SENT", result.code());
        assertNotNull(fixture.passwordResetEmailPort.lastResetUrl);
        assertTrue(fixture.passwordResetEmailPort.lastResetUrl.contains("token="));
    }

    @Test
    void shouldResetPasswordWithValidToken() {
        AuthFixture fixture = createFixture(student(Status.ACTIVE, true, true));
        fixture.service.forgotPassword(new ForgotPasswordCommand("student@example.com"));
        String rawToken = fixture.passwordResetEmailPort.lastResetUrl.substring(
                fixture.passwordResetEmailPort.lastResetUrl.indexOf("token=") + 6
        );

        ResetPasswordResult result = fixture.service.resetPassword(new ResetPasswordCommand(rawToken, "new-secret"));

        assertTrue(result.success());
        assertEquals("PASSWORD_RESET_SUCCESS", result.code());
        assertEquals("encoded:new-secret", fixture.saveUserPort.lastSavedUser.getPassword());
        assertTrue(fixture.passwordResetTokenPort.wasMarkedUsed(fixture.user.getId()));
    }

    @Test
    void shouldRejectExpiredResetToken() {
        AuthFixture fixture = createFixture(student(Status.ACTIVE, true, true));
        fixture.service.forgotPassword(new ForgotPasswordCommand("student@example.com"));
        String rawToken = fixture.passwordResetEmailPort.lastResetUrl.substring(
                fixture.passwordResetEmailPort.lastResetUrl.indexOf("token=") + 6
        );
        fixture.passwordResetTokenPort.expireActiveToken(fixture.user.getId());

        ResetPasswordResult result = fixture.service.resetPassword(new ResetPasswordCommand(rawToken, "new-secret"));

        assertFalse(result.success());
        assertEquals("TOKEN_EXPIRED", result.code());
    }

    @Test
    void shouldChangePasswordWhenCurrentPasswordMatches() {
        AuthFixture fixture = createFixture(student(Status.ACTIVE, true, true));

        ChangePasswordResult result = fixture.service.changePassword(
                new ChangePasswordCommand(fixture.user.getId(), "secret", "new-secret")
        );

        assertTrue(result.success());
        assertEquals("PASSWORD_CHANGED", result.code());
        assertEquals("encoded:new-secret", fixture.saveUserPort.lastSavedUser.getPassword());
    }

    @Test
    void shouldRejectChangePasswordWhenCurrentPasswordIsWrong() {
        AuthFixture fixture = createFixture(student(Status.ACTIVE, true, true));

        ChangePasswordResult result = fixture.service.changePassword(
                new ChangePasswordCommand(fixture.user.getId(), "wrong-secret", "new-secret")
        );

        assertFalse(result.success());
        assertEquals("CURRENT_PASSWORD_INCORRECT", result.code());
    }

    @Test
    void shouldAllowRejectedStudentToResubmitDocuments() {
        AuthFixture fixture = createFixture(student(Status.REJECTED, true, true));

        UploadStudentDocumentsResult result = fixture.service.upload(
                new UploadStudentDocumentsCommand(
                        1,
                        new MockMultipartFile("studentCard", "student-card.png", "image/png", new byte[]{1}),
                        new MockMultipartFile("nationalId", "national-id.png", "image/png", new byte[]{2})
                )
        );

        assertTrue(result.isSuccess());
        assertEquals("UPLOAD_DOCS_SUCCESS", result.getCode());
        assertNotNull(fixture.saveStudentPort.lastSavedStudent);
        assertEquals(Status.PENDING, fixture.saveStudentPort.lastSavedStudent.getStatus());
        assertEquals(2, fixture.saveStudentPort.lastSavedStudent.getResubmissionCount());
        assertNull(fixture.saveStudentPort.lastSavedStudent.getReviewReason());
        assertNull(fixture.saveStudentPort.lastSavedStudent.getReviewNotes());
    }

    @Test
    void shouldReturnOnboardingStatusWithReviewContext() {
        AuthFixture fixture = createFixture(student(Status.REJECTED, true, true));

        StudentOnboardingStatusResult result = fixture.service.getStatus(1);

        assertTrue(result.success());
        assertEquals("REJECTED", result.status());
        assertEquals("Document mismatch", result.reviewReason());
        assertTrue(result.hasUploadedDocuments());
        assertTrue(result.canUploadDocuments());
    }

    private AuthFixture createFixture(Student student) {
        User user = student != null
                ? student.getUser()
                : new User(1, "student@example.com", "secret", Role.STUDENT, true);
        return createFixture(user, student);
    }

    private AuthFixture createFixtureWithUser(User user) {
        return createFixture(user, null);
    }

    private AuthFixture createFixture(User user, Student student) {
        RecordingSaveUserPort saveUserPort = new RecordingSaveUserPort();
        RecordingSaveStudentPort saveStudentPort = new RecordingSaveStudentPort();
        RecordingPasswordResetEmailPort passwordResetEmailPort = new RecordingPasswordResetEmailPort();
        InMemoryPasswordResetTokenPort passwordResetTokenPort = new InMemoryPasswordResetTokenPort(user);
        PasswordEncoder passwordEncoder = new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                return "encoded:" + rawPassword;
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                return rawPassword.toString().contentEquals(encodedPassword);
            }
        };

        JwtTokenPort jwtTokenPort = new JwtTokenPort() {
            @Override
            public String generateAccessToken(Integer userId, String email, String role) {
                return "jwt-token";
            }

            @Override
            public boolean isTokenValid(String token) {
                return true;
            }

            @Override
            public Integer extractUserId(String token) {
                return user.getId();
            }

            @Override
            public String extractEmail(String token) {
                return user.getEmail();
            }

            @Override
            public String extractRole(String token) {
                return user.getRole().name();
            }
        };

        AuthUseCaseService service = new AuthUseCaseService(
                email -> {
                    if (user.getEmail().equals(email)) {
                        return Optional.of(user);
                    }
                    return Optional.empty();
                },
                userId -> {
                    if (user.getId().equals(userId)) {
                        return Optional.of(user);
                    }
                    return Optional.empty();
                },
                saveUserPort,
                new LoadStudentByIdPort() {
                    @Override
                    public Optional<Student> loadById(Integer studentId) {
                        return Optional.ofNullable(student);
                    }

                    @Override
                    public Optional<Student> loadByUserId(Integer userId) {
                        return Optional.ofNullable(student);
                    }
                },
                saveStudentPort,
                new NoOpLoadStudentTokenPort(),
                token -> token,
                passwordResetTokenPort,
                passwordResetTokenPort,
                departmentPort(),
                new NoOpVerifyEmailPort(),
                passwordResetEmailPort,
                new NoOpStoreStudentDocumentPort(),
                passwordEncoder,
                jwtTokenPort
        );

        return new AuthFixture(service, user, saveUserPort, saveStudentPort, passwordResetEmailPort, passwordResetTokenPort);
    }

    private LoadDepartmentPort departmentPort() {
        return departmentName -> Optional.of(new Department(1, departmentName));
    }

    private Student student(Status status, boolean verified, boolean withDocuments) {
        User user = new User(1, "student@example.com", "secret", Role.STUDENT, verified);
        return new Student(
                1,
                user,
                "Student One",
                "S0001",
                new Department(1, "Computer Science"),
                status,
                withDocuments ? "student-card.png" : null,
                withDocuments ? "national-id.png" : null,
                status == Status.REJECTED ? "Document mismatch" : null,
                status == Status.REJECTED ? "Please upload clearer identity photos." : null,
                status == Status.REJECTED ? 99 : null,
                status == Status.REJECTED ? LocalDateTime.now().minusDays(1) : null,
                status == Status.REJECTED ? 1 : 0
        );
    }

    private record AuthFixture(
            AuthUseCaseService service,
            User user,
            RecordingSaveUserPort saveUserPort,
            RecordingSaveStudentPort saveStudentPort,
            RecordingPasswordResetEmailPort passwordResetEmailPort,
            InMemoryPasswordResetTokenPort passwordResetTokenPort
    ) {
    }

    private static final class RecordingSaveUserPort implements SaveUserPort {
        private User lastSavedUser;

        @Override
        public User save(User user) {
            lastSavedUser = user;
            return user;
        }

        @Override
        public boolean existsByEmail(String email) {
            return false;
        }
    }

    private static final class RecordingSaveStudentPort implements SaveStudentPort {
        private Student lastSavedStudent;

        @Override
        public Student save(Student student) {
            lastSavedStudent = student;
            return student;
        }

        @Override
        public boolean existsByStudentCode(String studentCode) {
            return false;
        }
    }

    private static final class NoOpLoadStudentTokenPort implements LoadStudentTokenPort {
        @Override
        public Optional<StudentToken> loadByTokenAndDeleteFlg(String token, Integer deleteFlg) {
            return Optional.empty();
        }
    }

    private static final class NoOpVerifyEmailPort implements SendVerifyEmailPort {
        @Override
        public void sendVerifyEmail(String toEmail, String verifyUrl) {
        }
    }

    private static final class NoOpStoreStudentDocumentPort implements StoreStudentDocumentPort {
        @Override
        public String save(org.springframework.web.multipart.MultipartFile file, String prefix) {
            return prefix + "-path";
        }
    }

    private static final class RecordingPasswordResetEmailPort implements SendPasswordResetEmailPort {
        private String lastResetUrl;

        @Override
        public void sendPasswordResetEmail(String toEmail, String resetUrl) {
            lastResetUrl = resetUrl;
        }
    }

    private static final class InMemoryPasswordResetTokenPort
            implements LoadPasswordResetTokenPort, SavePasswordResetTokenPort {
        private final User user;
        private final Map<String, PasswordResetToken> tokensByHash = new HashMap<>();
        private boolean markedUsed;

        private InMemoryPasswordResetTokenPort(User user) {
            this.user = user;
        }

        @Override
        public Optional<PasswordResetToken> loadActiveByTokenHash(String tokenHash) {
            PasswordResetToken token = tokensByHash.get(tokenHash);
            if (token == null || token.getUsedAt() != null) {
                return Optional.empty();
            }
            return Optional.of(token);
        }

        @Override
        public PasswordResetToken save(PasswordResetToken token) {
            PasswordResetToken saved = new PasswordResetToken(
                    token.getId() == null ? tokensByHash.size() + 1 : token.getId(),
                    user,
                    token.getTokenHash(),
                    token.getExpiredAt(),
                    token.getCreatedAt(),
                    token.getUsedAt()
            );
            tokensByHash.put(saved.getTokenHash(), saved);
            return saved;
        }

        @Override
        public void markActiveTokensUsed(Integer userId) {
            markedUsed = true;
            tokensByHash.replaceAll((hash, token) -> {
                if (token.getUsedAt() != null) {
                    return token;
                }
                return new PasswordResetToken(
                        token.getId(),
                        token.getUser(),
                        token.getTokenHash(),
                        token.getExpiredAt(),
                        token.getCreatedAt(),
                        LocalDateTime.now()
                );
            });
        }

        private boolean wasMarkedUsed(Integer userId) {
            return markedUsed;
        }

        private void expireActiveToken(Integer userId) {
            tokensByHash.replaceAll((hash, token) -> new PasswordResetToken(
                    token.getId(),
                    token.getUser(),
                    token.getTokenHash(),
                    LocalDateTime.now().minusMinutes(1),
                    token.getCreatedAt(),
                    token.getUsedAt()
            ));
        }
    }
}
