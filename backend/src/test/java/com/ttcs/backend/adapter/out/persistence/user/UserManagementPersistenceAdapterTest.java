package com.ttcs.backend.adapter.out.persistence.user;

import com.ttcs.backend.adapter.out.persistence.DepartmentRepository;
import com.ttcs.backend.adapter.out.persistence.StatusEntity;
import com.ttcs.backend.adapter.out.persistence.UserRepository;
import com.ttcs.backend.adapter.out.persistence.admin.AdminRepository;
import com.ttcs.backend.adapter.out.persistence.department.DepartmentEntity;
import com.ttcs.backend.adapter.out.persistence.lecturer.LecturerRepository;
import com.ttcs.backend.adapter.out.persistence.role.RoleEntity;
import com.ttcs.backend.adapter.out.persistence.student.StudentEntity;
import com.ttcs.backend.adapter.out.persistence.student.StudentRepository;
import com.ttcs.backend.application.domain.model.Department;
import com.ttcs.backend.application.domain.model.ManagedUser;
import com.ttcs.backend.application.domain.model.Role;
import com.ttcs.backend.application.domain.model.Status;
import com.ttcs.backend.application.domain.model.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserManagementPersistenceAdapterTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private LecturerRepository lecturerRepository;

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private UserManagementPersistenceAdapter adapter;

    @Test
    void shouldLoadStudentProfileByUserIdWhenLoadingManagedUser() {
        UserEntity user = userEntity(2, RoleEntity.STUDENT);
        StudentEntity student = studentEntity(99, user, "S0002");

        when(userRepository.findById(2)).thenReturn(Optional.of(user));
        when(studentRepository.findByUserId(2)).thenReturn(Optional.of(student));

        ManagedUser result = adapter.loadById(2).orElseThrow();

        assertEquals(2, result.getUser().getId());
        assertEquals("S0002", result.getStudentCode());
        assertEquals(Status.ACTIVE, result.getStudentStatus());
        verify(studentRepository).findByUserId(2);
        verify(studentRepository, never()).findById(anyInt());
    }

    @Test
    void shouldMapStudentProfilesByUserIdWhenLoadingAllManagedUsers() {
        UserEntity user = userEntity(2, RoleEntity.STUDENT);
        StudentEntity student = studentEntity(99, user, "S0002");

        when(userRepository.findAll()).thenReturn(List.of(user));
        when(studentRepository.findAll()).thenReturn(List.of(student));
        when(lecturerRepository.findAll()).thenReturn(List.of());
        when(adminRepository.findAll()).thenReturn(List.of());

        List<ManagedUser> result = adapter.loadAll();

        assertEquals(1, result.size());
        assertEquals(2, result.getFirst().getUser().getId());
        assertEquals("S0002", result.getFirst().getStudentCode());
    }

    @Test
    void shouldUpdateStudentProfileByUserIdWhenSavingManagedStudent() {
        UserEntity user = userEntity(2, RoleEntity.STUDENT);
        StudentEntity student = studentEntity(99, user, "S0002");
        DepartmentEntity department = new DepartmentEntity(3, "Information Systems");

        when(userRepository.findById(2)).thenReturn(Optional.of(user));
        when(studentRepository.findByUserId(2)).thenReturn(Optional.of(student));
        when(departmentRepository.findById(3)).thenReturn(Optional.of(department));

        adapter.save(new ManagedUser(
                new User(2, "student.updated@university.edu", "secret", Role.STUDENT, true),
                "Updated Student",
                new Department(3, "Information Systems"),
                "S0099",
                null,
                Status.ACTIVE
        ));

        assertEquals("Updated Student", student.getName());
        assertEquals("S0099", student.getStudentCode());
        assertEquals(department, student.getDepartment());
        verify(studentRepository).findByUserId(2);
        verify(studentRepository, never()).findById(anyInt());
        verify(studentRepository).save(student);
    }

    @Test
    void shouldCheckStudentCodeUniquenessAgainstUserId() {
        when(studentRepository.existsByStudentCodeAndUser_IdNot("S0099", 2)).thenReturn(true);

        boolean result = adapter.existsStudentCodeExcludingUserId("S0099", 2);

        assertEquals(true, result);
        verify(studentRepository).existsByStudentCodeAndUser_IdNot("S0099", 2);
        verify(studentRepository, never()).findAll();
    }

    private UserEntity userEntity(Integer userId, RoleEntity role) {
        return new UserEntity(userId, "student@university.edu", "secret", role, true);
    }

    private StudentEntity studentEntity(Integer studentId, UserEntity user, String studentCode) {
        return new StudentEntity(
                studentId,
                user,
                "Student Demo",
                studentCode,
                new DepartmentEntity(1, "Computer Science"),
                StatusEntity.ACTIVE,
                null,
                null,
                null,
                null,
                null,
                null,
                0
        );
    }
}
