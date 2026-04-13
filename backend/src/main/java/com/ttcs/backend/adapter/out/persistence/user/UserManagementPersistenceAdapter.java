package com.ttcs.backend.adapter.out.persistence.user;

import com.ttcs.backend.adapter.out.persistence.DepartmentRepository;
import com.ttcs.backend.adapter.out.persistence.UserRepository;
import com.ttcs.backend.adapter.out.persistence.admin.AdminEntity;
import com.ttcs.backend.adapter.out.persistence.admin.AdminRepository;
import com.ttcs.backend.adapter.out.persistence.department.DepartmentMapper;
import com.ttcs.backend.adapter.out.persistence.student.StudentEntity;
import com.ttcs.backend.adapter.out.persistence.student.StudentMapper;
import com.ttcs.backend.adapter.out.persistence.student.StudentRepository;
import com.ttcs.backend.adapter.out.persistence.teacher.TeacherEntity;
import com.ttcs.backend.adapter.out.persistence.teacher.TeacherMapper;
import com.ttcs.backend.adapter.out.persistence.teacher.TeacherRepository;
import com.ttcs.backend.adapter.out.persistence.user.UserMapper;
import com.ttcs.backend.application.domain.model.Department;
import com.ttcs.backend.application.domain.model.ManagedUser;
import com.ttcs.backend.application.domain.model.Role;
import com.ttcs.backend.application.port.out.admin.ManageUserPort;
import com.ttcs.backend.common.PersistenceAdapter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@PersistenceAdapter
@RequiredArgsConstructor
public class UserManagementPersistenceAdapter implements ManageUserPort {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final AdminRepository adminRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    public List<ManagedUser> loadAll() {
        Map<Integer, StudentEntity> students = studentRepository.findAll().stream()
                .collect(Collectors.toMap(StudentEntity::getId, Function.identity()));
        Map<Integer, TeacherEntity> teachers = teacherRepository.findAll().stream()
                .collect(Collectors.toMap(TeacherEntity::getId, Function.identity()));
        Map<Integer, AdminEntity> admins = adminRepository.findAll().stream()
                .collect(Collectors.toMap(AdminEntity::getId, Function.identity()));

        return userRepository.findAll().stream()
                .map(user -> toManagedUser(user, students.get(user.getId()), teachers.get(user.getId()), admins.get(user.getId())))
                .toList();
    }

    @Override
    public Optional<ManagedUser> loadById(Integer userId) {
        return userRepository.findById(userId)
                .map(user -> toManagedUser(
                        user,
                        studentRepository.findById(userId).orElse(null),
                        teacherRepository.findById(userId).orElse(null),
                        adminRepository.findById(userId).orElse(null)
                ));
    }

    @Override
    public Optional<Department> loadDepartmentById(Integer departmentId) {
        return departmentRepository.findById(departmentId).map(DepartmentMapper::toDomain);
    }

    @Override
    public boolean existsByEmailExcludingUserId(String email, Integer userId) {
        return userRepository.findByEmail(email)
                .map(item -> !item.getId().equals(userId))
                .orElse(false);
    }

    @Override
    public boolean existsStudentCodeExcludingUserId(String studentCode, Integer userId) {
        return studentRepository.findAll().stream()
                .anyMatch(item -> item.getStudentCode().equals(studentCode) && !item.getId().equals(userId));
    }

    @Override
    public boolean existsTeacherCodeExcludingUserId(String teacherCode, Integer userId) {
        return teacherRepository.existsByTeacherCodeAndIdNot(teacherCode, userId);
    }

    @Override
    public void save(ManagedUser managedUser) {
        UserEntity userEntity = userRepository.findById(managedUser.getUser().getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + managedUser.getUser().getId()));
        userEntity.setEmail(managedUser.getUser().getEmail());
        userEntity.setPassword(managedUser.getUser().getPassword());
        userEntity.setRole(com.ttcs.backend.adapter.out.persistence.role.RoleMapper.toEntity(managedUser.getUser().getRole()));
        userEntity.setVerified(managedUser.getUser().getVerified());
        userRepository.save(userEntity);

        if (managedUser.getUser().getRole() == Role.STUDENT) {
            StudentEntity studentEntity = studentRepository.findById(managedUser.getUser().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Student not found: " + managedUser.getUser().getId()));
            studentEntity.setName(managedUser.getName());
            studentEntity.setStudentCode(managedUser.getStudentCode());
            if (managedUser.getDepartment() != null) {
                studentEntity.setDepartment(departmentRepository.findById(managedUser.getDepartment().getId())
                        .orElseThrow(() -> new IllegalArgumentException("Department not found: " + managedUser.getDepartment().getId())));
            }
            studentRepository.save(studentEntity);
            return;
        }

        if (managedUser.getUser().getRole() == Role.TEACHER) {
            TeacherEntity teacherEntity = teacherRepository.findById(managedUser.getUser().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Teacher not found: " + managedUser.getUser().getId()));
            teacherEntity.setName(managedUser.getName());
            teacherEntity.setTeacherCode(managedUser.getTeacherCode());
            if (managedUser.getDepartment() != null) {
                teacherEntity.setDepartment(departmentRepository.findById(managedUser.getDepartment().getId())
                        .orElseThrow(() -> new IllegalArgumentException("Department not found: " + managedUser.getDepartment().getId())));
            }
            teacherRepository.save(teacherEntity);
            return;
        }

        AdminEntity adminEntity = adminRepository.findById(managedUser.getUser().getId())
                .orElseThrow(() -> new IllegalArgumentException("Admin not found: " + managedUser.getUser().getId()));
        adminEntity.setName(managedUser.getName());
        adminRepository.save(adminEntity);
    }

    private ManagedUser toManagedUser(UserEntity user, StudentEntity student, TeacherEntity teacher, AdminEntity admin) {
        return switch (user.getRole()) {
            case STUDENT -> new ManagedUser(
                    UserMapper.toDomain(user),
                    student != null ? student.getName() : null,
                    student != null && student.getDepartment() != null ? DepartmentMapper.toDomain(student.getDepartment()) : null,
                    student != null ? student.getStudentCode() : null,
                    null,
                    student != null ? StudentMapper.toDomain(student).getStatus() : null
            );
            case TEACHER -> new ManagedUser(
                    UserMapper.toDomain(user),
                    teacher != null ? teacher.getName() : null,
                    teacher != null && teacher.getDepartment() != null ? DepartmentMapper.toDomain(teacher.getDepartment()) : null,
                    null,
                    teacher != null ? teacher.getTeacherCode() : null,
                    null
            );
            case ADMIN -> new ManagedUser(
                    UserMapper.toDomain(user),
                    admin != null ? admin.getName() : null,
                    null,
                    null,
                    null,
                    null
            );
        };
    }
}
