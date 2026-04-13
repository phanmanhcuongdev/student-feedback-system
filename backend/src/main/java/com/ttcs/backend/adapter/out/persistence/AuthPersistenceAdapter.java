package com.ttcs.backend.adapter.out.persistence;

import com.ttcs.backend.adapter.out.persistence.department.DepartmentMapper;
import com.ttcs.backend.adapter.out.persistence.passwordresettoken.PasswordResetTokenEntity;
import com.ttcs.backend.adapter.out.persistence.passwordresettoken.PasswordResetTokenRepository;
import com.ttcs.backend.adapter.out.persistence.StatusEntity;
import com.ttcs.backend.adapter.out.persistence.student.StudentEntity;
import com.ttcs.backend.adapter.out.persistence.student.StudentMapper;
import com.ttcs.backend.adapter.out.persistence.student.StudentRepository;
import com.ttcs.backend.adapter.out.persistence.user.UserEntity;
import com.ttcs.backend.adapter.out.persistence.user.UserMapper;
import com.ttcs.backend.application.domain.model.Department;
import com.ttcs.backend.application.domain.model.PasswordResetToken;
import com.ttcs.backend.application.domain.model.Student;
import com.ttcs.backend.application.domain.model.StudentToken;
import com.ttcs.backend.application.domain.model.User;
import com.ttcs.backend.application.port.out.auth.LoadDepartmentPort;
import com.ttcs.backend.application.port.out.auth.LoadPasswordResetTokenPort;
import com.ttcs.backend.application.port.out.auth.LoadStudentByIdPort;
import com.ttcs.backend.application.port.out.auth.LoadStudentTokenPort;
import com.ttcs.backend.application.port.out.auth.LoadUserByEmailPort;
import com.ttcs.backend.application.port.out.auth.SavePasswordResetTokenPort;
import com.ttcs.backend.application.port.out.auth.SaveStudentPort;
import com.ttcs.backend.application.port.out.auth.SaveStudentTokenPort;
import com.ttcs.backend.application.port.out.auth.SaveUserPort;
import com.ttcs.backend.application.port.out.admin.LoadPendingStudentsPort;
import com.ttcs.backend.common.PersistenceAdapter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@PersistenceAdapter
@RequiredArgsConstructor
public class AuthPersistenceAdapter implements
        LoadUserByEmailPort,
        SaveUserPort,
        LoadStudentByIdPort,
        SaveStudentPort,
        LoadStudentTokenPort,
        SaveStudentTokenPort,
        LoadDepartmentPort,
        LoadPendingStudentsPort,
        LoadPasswordResetTokenPort,
        SavePasswordResetTokenPort {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final StudentTokenRepository studentTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    public Optional<User> loadByEmail(String email) {
        return userRepository.findByEmail(email).map(UserMapper::toDomain);
    }

    @Override
    public User save(User user) {
        UserEntity saved = userRepository.save(UserMapper.toEntity(user));
        return UserMapper.toDomain(saved);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public Optional<Student> loadById(Integer studentId) {
        return studentRepository.findById(studentId).map(StudentMapper::toDomain);
    }

    @Override
    public Optional<Student> loadByUserId(Integer userId) {
        return studentRepository.findByUserId(userId).map(StudentMapper::toDomain);
    }

    @Override
    public Student save(Student student) {
        StudentEntity saved = studentRepository.save(StudentMapper.toEntity(student));
        return StudentMapper.toDomain(saved);
    }

    @Override
    public boolean existsByStudentCode(String studentCode) {
        return studentRepository.existsByStudentCode(studentCode);
    }

    @Override
    public Optional<StudentToken> loadByTokenAndDeleteFlg(String token, Integer deleteFlg) {
        return studentTokenRepository.findByTokenAndDeleteFlg(token, deleteFlg)
                .map(this::toDomainToken);
    }

    @Override
    public StudentToken save(StudentToken token) {
        StudentTokenEntity entity = toEntityToken(token);
        StudentTokenEntity saved = studentTokenRepository.save(entity);
        return toDomainToken(saved);
    }

    @Override
    public Optional<Department> loadByName(String departmentName) {
        return departmentRepository.findByName(departmentName).map(DepartmentMapper::toDomain);
    }

    @Override
    public List<Student> loadPendingStudents() {
        return studentRepository.findByStatusOrderByIdAsc(StatusEntity.PENDING).stream()
                .map(StudentMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<PasswordResetToken> loadActiveByTokenHash(String tokenHash) {
        return passwordResetTokenRepository.findByTokenHashAndUsedAtIsNull(tokenHash)
                .map(this::toDomainPasswordResetToken);
    }

    @Override
    public PasswordResetToken save(PasswordResetToken token) {
        PasswordResetTokenEntity entity = toEntityPasswordResetToken(token);
        PasswordResetTokenEntity saved = passwordResetTokenRepository.save(entity);
        return toDomainPasswordResetToken(saved);
    }

    @Override
    public void markActiveTokensUsed(Integer userId) {
        passwordResetTokenRepository.markActiveTokensUsed(userId, LocalDateTime.now());
    }

    private StudentToken toDomainToken(StudentTokenEntity entity) {
        return new StudentToken(
                entity.getId(),
                StudentMapper.toDomain(entity.getStudent()),
                entity.getToken(),
                entity.getExpiredAt(),
                entity.getCreatedAt(),
                entity.getDeleteFlg()
        );
    }

    private StudentTokenEntity toEntityToken(StudentToken domain) {
        StudentTokenEntity entity = new StudentTokenEntity();
        entity.setId(domain.getId());
        entity.setToken(domain.getToken());
        entity.setExpiredAt(domain.getExpiredAt());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setDeleteFlg(domain.getDeleteFlg());

        Integer studentId = domain.getStudent() != null ? domain.getStudent().getId() : null;
        if (studentId == null) {
            throw new IllegalArgumentException("Student id is required when saving StudentToken");
        }

        StudentEntity studentEntity = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));
        entity.setStudent(studentEntity);

        return entity;
    }

    private PasswordResetToken toDomainPasswordResetToken(PasswordResetTokenEntity entity) {
        return new PasswordResetToken(
                entity.getId(),
                UserMapper.toDomain(entity.getUser()),
                entity.getTokenHash(),
                entity.getExpiredAt(),
                entity.getCreatedAt(),
                entity.getUsedAt()
        );
    }

    private PasswordResetTokenEntity toEntityPasswordResetToken(PasswordResetToken domain) {
        PasswordResetTokenEntity entity = new PasswordResetTokenEntity();
        entity.setId(domain.getId());
        entity.setTokenHash(domain.getTokenHash());
        entity.setExpiredAt(domain.getExpiredAt());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUsedAt(domain.getUsedAt());

        Integer userId = domain.getUser() != null ? domain.getUser().getId() : null;
        if (userId == null) {
            throw new IllegalArgumentException("User id is required when saving PasswordResetToken");
        }

        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        entity.setUser(userEntity);
        return entity;
    }
}
