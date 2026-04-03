package com.ttcs.backend.application.domain.service;

import com.ttcs.backend.adapter.in.web.dto.ApproveStudentResponse;
import com.ttcs.backend.adapter.in.web.dto.LoginRequest;
import com.ttcs.backend.adapter.in.web.dto.LoginResponse;
import com.ttcs.backend.adapter.in.web.dto.RejectStudentResponse;
import com.ttcs.backend.adapter.in.web.dto.RegisterStudentRequest;
import com.ttcs.backend.adapter.in.web.dto.RegisterStudentResponse;
import com.ttcs.backend.adapter.in.web.dto.UploadDocumentsResponse;
import com.ttcs.backend.adapter.in.web.dto.VerifyEmailResponse;
import com.ttcs.backend.adapter.out.persistence.DepartmentEntity;
import com.ttcs.backend.adapter.out.persistence.DepartmentRepository;
import com.ttcs.backend.adapter.out.persistence.RoleEntity;
import com.ttcs.backend.adapter.out.persistence.StatusEntity;
import com.ttcs.backend.adapter.out.persistence.StudentEntity;
import com.ttcs.backend.adapter.out.persistence.StudentRepository;
import com.ttcs.backend.adapter.out.persistence.StudentTokenEntity;
import com.ttcs.backend.adapter.out.persistence.StudentTokenRepository;
import com.ttcs.backend.adapter.out.persistence.UserEntity;
import com.ttcs.backend.adapter.out.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final StudentTokenRepository studentTokenRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final StudentDocumentStorageService storageService;

    @Value("${app.verify.base-url:http://localhost:8080}")
    private String verifyBaseUrl;

    @Transactional
    public RegisterStudentResponse registerStudent(RegisterStudentRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return new RegisterStudentResponse(false, "EMAIL_ALREADY_USED", "Email da duoc su dung");
        }

        if (studentRepository.existsByStudentCode(request.getStudentCode())) {
            return new RegisterStudentResponse(false, "STUDENT_CODE_ALREADY_USED", "Ma sinh vien da duoc su dung");
        }

        DepartmentEntity department = departmentRepository.findByName(request.getDepartmentName()).orElse(null);
        if (department == null) {
            return new RegisterStudentResponse(false, "DEPARTMENT_NOT_FOUND", "Khoa khong ton tai");
        }

        UserEntity user = new UserEntity();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(RoleEntity.STUDENT);
        user.setVerified(false); // tạm giữ tương thích schema cũ
        UserEntity savedUser = userRepository.save(user);

        StudentEntity student = new StudentEntity();
        student.setId(savedUser.getId());
        student.setName(request.getName());
        student.setStatus(StatusEntity.EMAIL_UNVERIFIED);
        student.setStudentCode(request.getStudentCode());
        student.setDepartment(department);
        studentRepository.save(student);

        StudentTokenEntity token = new StudentTokenEntity();
        token.setStudent(student);
        token.setToken(UUID.randomUUID().toString());
        token.setExpiredAt(LocalDateTime.now().plusHours(24));
        token.setCreatedAt(LocalDateTime.now());
        token.setDeleteFlg(0);
        StudentTokenEntity savedToken = studentTokenRepository.save(token);

        String verifyUrl = verifyBaseUrl + "/api/auth/verify-email?token=" + savedToken.getToken();
        mailService.sendVerifyEmail(savedUser.getEmail(), verifyUrl);

        return new RegisterStudentResponse(true, "REGISTER_SUCCESS", "Dang ky thanh cong. Vui long kiem tra email de xac nhan.");
    }

    @Transactional
    public VerifyEmailResponse verifyEmail(String token) {
        StudentTokenEntity tokenEntity = studentTokenRepository.findByTokenAndDeleteFlg(token, 0).orElse(null);
        if (tokenEntity == null) {
            return new VerifyEmailResponse(false, "TOKEN_INVALID", "Token khong hop le hoac da duoc su dung");
        }

        if (LocalDateTime.now().isAfter(tokenEntity.getExpiredAt())) {
            return new VerifyEmailResponse(false, "TOKEN_EXPIRED", "Token da het hieu luc");
        }

        StudentEntity student = tokenEntity.getStudent();
        if (student.getStatus() == StatusEntity.EMAIL_UNVERIFIED) {
            student.setStatus(StatusEntity.PENDING);
            studentRepository.save(student);
        } else if (student.getStatus() == StatusEntity.ACTIVE || student.getStatus() == StatusEntity.PENDING) {
            tokenEntity.setDeleteFlg(1);
            return new VerifyEmailResponse(false, "ALREADY_VERIFIED", "Email da duoc xac nhan truoc do");
        }

        tokenEntity.setDeleteFlg(1); // mark used
        studentTokenRepository.save(tokenEntity);

        return new VerifyEmailResponse(true, "VERIFY_SUCCESS", "Xac nhan email thanh cong. Vui long gui 2 anh minh chung.");
    }

    @Transactional
    public UploadDocumentsResponse uploadDocuments(Integer studentId, MultipartFile studentCard, MultipartFile nationalId) {
        StudentEntity student = studentRepository.findById(studentId).orElse(null);
        if (student == null) {
            return new UploadDocumentsResponse(false, "STUDENT_NOT_FOUND", "Khong tim thay sinh vien");
        }

        if (student.getStatus() == StatusEntity.EMAIL_UNVERIFIED) {
            return new UploadDocumentsResponse(false, "EMAIL_NOT_VERIFIED", "Ban can xac nhan email truoc khi gui minh chung");
        }

        String cardPath = storageService.save(studentCard, "student-card");
        String cccdPath = storageService.save(nationalId, "national-id");

        student.setStudentCardImageUrl(cardPath);
        student.setNationalIdImageUrl(cccdPath);
        student.setStatus(StatusEntity.PENDING);
        studentRepository.save(student);

        return new UploadDocumentsResponse(true, "UPLOAD_DOCS_SUCCESS", "Gui minh chung thanh cong. Tai khoan dang cho admin duyet.");
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        UserEntity user = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return LoginResponse.fail("INVALID_CREDENTIALS", "Sai email hoac mat khau");
        }

        String studentStatus = null;
        if (user.getRole() == RoleEntity.STUDENT) {
            StudentEntity student = studentRepository.findById(user.getId()).orElse(null);
            if (student == null) {
                return LoginResponse.fail("STUDENT_PROFILE_NOT_FOUND", "Khong tim thay thong tin sinh vien");
            }
            studentStatus = student.getStatus().name();
        }

        return LoginResponse.success(user.getId(), user.getRole().name(), studentStatus);
    }

    @Transactional
    public ApproveStudentResponse approveStudent(Integer studentId) {
        StudentEntity student = studentRepository.findById(studentId).orElse(null);
        if (student == null) {
            return new ApproveStudentResponse(false, "STUDENT_NOT_FOUND", "Khong tim thay sinh vien");
        }

        if (student.getStudentCardImageUrl() == null || student.getNationalIdImageUrl() == null) {
            return new ApproveStudentResponse(false, "MISSING_DOCUMENTS", "Sinh vien chua upload du 2 anh minh chung");
        }

        student.setStatus(StatusEntity.ACTIVE);
        studentRepository.save(student);
        return new ApproveStudentResponse(true, "APPROVE_SUCCESS", "Duyet sinh vien thanh cong");
    }

    @Transactional
    public RejectStudentResponse rejectStudent(Integer studentId) {
        StudentEntity student = studentRepository.findById(studentId).orElse(null);
        if (student == null) {
            return new RejectStudentResponse(false, "STUDENT_NOT_FOUND", "Khong tim thay sinh vien");
        }

        student.setStatus(StatusEntity.REJECTED);
        studentRepository.save(student);
        return new RejectStudentResponse(true, "REJECT_SUCCESS", "Tu choi sinh vien thanh cong");
    }
}
