package com.ttcs.backend.application.domain.service;

import com.ttcs.backend.application.domain.model.Department;
import com.ttcs.backend.application.domain.model.Role;
import com.ttcs.backend.application.domain.model.Status;
import com.ttcs.backend.application.domain.model.Student;
import com.ttcs.backend.application.domain.model.StudentToken;
import com.ttcs.backend.application.domain.model.User;
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
import com.ttcs.backend.application.port.out.auth.*;
import com.ttcs.backend.common.UseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@UseCase
@RequiredArgsConstructor
public class AuthUseCaseService implements
        RegisterStudentUseCase,
        LoginUseCase,
        VerifyEmailUseCase,
        UploadStudentDocumentsUseCase {

    private final LoadUserByEmailPort loadUserByEmailPort;
    private final SaveUserPort saveUserPort;

    private final LoadStudentByIdPort loadStudentByIdPort;
    private final SaveStudentPort saveStudentPort;

    private final LoadStudentTokenPort loadStudentTokenPort;
    private final SaveStudentTokenPort saveStudentTokenPort;

    private final LoadDepartmentPort loadDepartmentPort;
    private final SendVerifyEmailPort sendVerifyEmailPort;
    private final StoreStudentDocumentPort storeStudentDocumentPort;

    private final PasswordEncoder passwordEncoder;

    private final JwtTokenPort jwtTokenPort;

    @Value("${app.verify.base-url:http://localhost:8080}")
    private String verifyBaseUrl;

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

        String verifyUrl = verifyBaseUrl + "/api/auth/verify-email?token=" + savedToken.getToken();
        sendVerifyEmailPort.sendVerifyEmail(savedUser.getEmail(), verifyUrl);

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

        String studentStatus = null;
        if (user.getRole() == Role.STUDENT) {
            Student student = loadStudentByIdPort.loadByUserId(user.getId()).orElse(null);
            if (student == null) {
                return LoginResult.fail("STUDENT_PROFILE_NOT_FOUND", "Khong tim thay thong tin sinh vien");
            }
            studentStatus = student.getStatus().name();
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
                    Status.PENDING,
                    student.getStudentCardImageUrl(),
                    student.getNationalIdImageUrl()
            );
            saveStudentPort.save(updatedStudent);
        } else if (student.getStatus() == Status.ACTIVE || student.getStatus() == Status.PENDING) {
            StudentToken usedToken = new StudentToken(
                    token.getId(),
                    token.getStudent(),
                    token.getToken(),
                    token.getExpiredAt(),
                    token.getCreatedAt(),
                    1
            );
            saveStudentTokenPort.save(usedToken);
            return VerifyEmailResult.fail("ALREADY_VERIFIED", "Email da duoc xac nhan truoc do");
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

        return VerifyEmailResult.ok();
    }

    @Override
    @Transactional
    public UploadStudentDocumentsResult upload(UploadStudentDocumentsCommand command) {
        if (command == null
                || command.studentId() == null
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
                    "Ban can xac nhan email truoc khi gui minh chung"
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

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
