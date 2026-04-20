package com.ttcs.backend.adapter.out.persistence.user;

import com.ttcs.backend.adapter.out.persistence.DepartmentRepository;
import com.ttcs.backend.adapter.out.persistence.UserRepository;
import com.ttcs.backend.adapter.out.persistence.admin.AdminEntity;
import com.ttcs.backend.adapter.out.persistence.admin.AdminRepository;
import com.ttcs.backend.adapter.out.persistence.department.DepartmentMapper;
import com.ttcs.backend.adapter.out.persistence.student.StudentEntity;
import com.ttcs.backend.adapter.out.persistence.student.StudentMapper;
import com.ttcs.backend.adapter.out.persistence.student.StudentRepository;
import com.ttcs.backend.adapter.out.persistence.lecturer.LecturerEntity;
import com.ttcs.backend.adapter.out.persistence.lecturer.LecturerMapper;
import com.ttcs.backend.adapter.out.persistence.lecturer.LecturerRepository;
import com.ttcs.backend.adapter.out.persistence.user.UserMapper;
import com.ttcs.backend.application.domain.model.Department;
import com.ttcs.backend.application.domain.model.ManagedUser;
import com.ttcs.backend.application.domain.model.Role;
import com.ttcs.backend.application.port.out.admin.ManagedUserMetrics;
import com.ttcs.backend.application.port.out.admin.ManagedUserSearchItem;
import com.ttcs.backend.application.port.out.admin.ManagedUserSearchPage;
import com.ttcs.backend.application.port.out.admin.ManageUserPort;
import com.ttcs.backend.application.port.out.admin.ManageUsersQuery;
import com.ttcs.backend.common.PersistenceAdapter;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;

import java.math.BigInteger;
import java.util.ArrayList;
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
    private final LecturerRepository lecturerRepository;
    private final AdminRepository adminRepository;
    private final DepartmentRepository departmentRepository;
    private final EntityManager entityManager;

    private static final String USER_SELECT = """
            FROM [User] u
            LEFT JOIN Student s ON s.user_id = u.user_id
            LEFT JOIN Lecturer l ON l.user_id = u.user_id
            LEFT JOIN Admin a ON a.user_id = u.user_id
            LEFT JOIN Department d ON d.dept_id = COALESCE(s.dept_id, l.dept_id)
            """;

    @Override
    public List<ManagedUser> loadAll() {
        Map<Integer, StudentEntity> students = studentRepository.findAll().stream()
                .filter(student -> student.getUser() != null && student.getUser().getId() != null)
                .collect(Collectors.toMap(student -> student.getUser().getId(), Function.identity()));
        Map<Integer, LecturerEntity> lecturers = lecturerRepository.findAll().stream()
                .collect(Collectors.toMap(LecturerEntity::getId, Function.identity()));
        Map<Integer, AdminEntity> admins = adminRepository.findAll().stream()
                .collect(Collectors.toMap(AdminEntity::getId, Function.identity()));

        return userRepository.findAll().stream()
                .map(user -> toManagedUser(user, students.get(user.getId()), lecturers.get(user.getId()), admins.get(user.getId())))
                .toList();
    }

    @Override
    public ManagedUserSearchPage loadPage(ManageUsersQuery query) {
        int page = Math.max(query.page(), 0);
        int size = Math.min(Math.max(query.size(), 1), 100);

        String whereClause = buildWhereClause(query);
        String orderClause = buildOrderClause(query.sortBy(), query.sortDir());

        Query itemsQuery = entityManager.createNativeQuery("""
                SELECT
                    u.user_id,
                    u.email,
                    u.role,
                    COALESCE(s.name, l.name, a.name),
                    d.dept_id,
                    d.name,
                    s.status,
                    u.verify,
                    s.student_code,
                    l.lecturer_code
                """
                + USER_SELECT
                + whereClause
                + orderClause
                + " OFFSET :offset ROWS FETCH NEXT :size ROWS ONLY");
        applyQueryParameters(itemsQuery, query);
        itemsQuery.setParameter("offset", page * size);
        itemsQuery.setParameter("size", size);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = itemsQuery.getResultList();
        List<ManagedUserSearchItem> items = rows.stream()
                .map(this::toSearchItem)
                .toList();

        Query countQuery = entityManager.createNativeQuery("SELECT COUNT(*) " + USER_SELECT + whereClause);
        applyQueryParameters(countQuery, query);
        long totalElements = ((Number) countQuery.getSingleResult()).longValue();
        int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / size);

        Query metricsQuery = entityManager.createNativeQuery("""
                SELECT
                    COUNT(*) AS totalUsers,
                    SUM(CASE WHEN u.role = 'STUDENT' THEN 1 ELSE 0 END) AS totalStudents,
                    SUM(CASE WHEN u.role = 'LECTURER' THEN 1 ELSE 0 END) AS totalLecturers,
                    SUM(CASE WHEN u.role = 'ADMIN' THEN 1 ELSE 0 END) AS totalAdmins,
                    SUM(CASE WHEN u.verify = 0 THEN 1 ELSE 0 END) AS totalInactive,
                    SUM(CASE WHEN s.status = 'PENDING' THEN 1 ELSE 0 END) AS totalPending
                """
                + USER_SELECT);
        Object[] metricsRow = (Object[]) metricsQuery.getSingleResult();

        return new ManagedUserSearchPage(
                items,
                page,
                size,
                totalElements,
                totalPages,
                new ManagedUserMetrics(
                        getLong(metricsRow[0]),
                        getLong(metricsRow[1]),
                        getLong(metricsRow[2]),
                        getLong(metricsRow[3]),
                        getLong(metricsRow[4]),
                        getLong(metricsRow[5])
                )
        );
    }

    @Override
    public List<Department> loadDepartments() {
        return departmentRepository.findAll(Sort.by(Sort.Direction.ASC, "name")).stream()
                .map(DepartmentMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<ManagedUser> loadById(Integer userId) {
        return userRepository.findById(userId)
                .map(user -> toManagedUser(
                        user,
                        studentRepository.findByUserId(userId).orElse(null),
                        lecturerRepository.findById(userId).orElse(null),
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
        return studentRepository.existsByStudentCodeAndUser_IdNot(studentCode, userId);
    }

    @Override
    public boolean existsLecturerCodeExcludingUserId(String lecturerCode, Integer userId) {
        return lecturerRepository.existsByLecturerCodeAndIdNot(lecturerCode, userId);
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
            StudentEntity studentEntity = studentRepository.findByUserId(managedUser.getUser().getId())
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

        if (managedUser.getUser().getRole() == Role.LECTURER) {
            LecturerEntity lecturerEntity = lecturerRepository.findById(managedUser.getUser().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Lecturer not found: " + managedUser.getUser().getId()));
            lecturerEntity.setName(managedUser.getName());
            lecturerEntity.setLecturerCode(managedUser.getLecturerCode());
            if (managedUser.getDepartment() != null) {
                lecturerEntity.setDepartment(departmentRepository.findById(managedUser.getDepartment().getId())
                        .orElseThrow(() -> new IllegalArgumentException("Department not found: " + managedUser.getDepartment().getId())));
            }
            lecturerRepository.save(lecturerEntity);
            return;
        }

        AdminEntity adminEntity = adminRepository.findById(managedUser.getUser().getId())
                .orElseThrow(() -> new IllegalArgumentException("Admin not found: " + managedUser.getUser().getId()));
        adminEntity.setName(managedUser.getName());
        adminRepository.save(adminEntity);
    }

    private ManagedUser toManagedUser(UserEntity user, StudentEntity student, LecturerEntity lecturer, AdminEntity admin) {
        return switch (user.getRole()) {
            case STUDENT -> new ManagedUser(
                    UserMapper.toDomain(user),
                    student != null ? student.getName() : null,
                    student != null && student.getDepartment() != null ? DepartmentMapper.toDomain(student.getDepartment()) : null,
                    student != null ? student.getStudentCode() : null,
                    null,
                    student != null ? StudentMapper.toDomain(student).getStatus() : null
            );
            case LECTURER -> new ManagedUser(
                    UserMapper.toDomain(user),
                    lecturer != null ? lecturer.getName() : null,
                    lecturer != null && lecturer.getDepartment() != null ? DepartmentMapper.toDomain(lecturer.getDepartment()) : null,
                    null,
                    lecturer != null ? lecturer.getLecturerCode() : null,
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

    private ManagedUserSearchItem toSearchItem(Object[] row) {
        return new ManagedUserSearchItem(
                ((Number) row[0]).intValue(),
                (String) row[1],
                row[2] != null ? row[2].toString() : null,
                (String) row[3],
                row[4] != null ? ((Number) row[4]).intValue() : null,
                (String) row[5],
                row[6] != null ? row[6].toString() : null,
                row[7] instanceof Boolean bool ? bool : ((Number) row[7]).intValue() == 1,
                (String) row[8],
                (String) row[9]
        );
    }

    private void applyQueryParameters(Query query, ManageUsersQuery request) {
        if (request.keyword() != null && !request.keyword().isBlank()) {
            query.setParameter("keyword", "%" + request.keyword().trim().toLowerCase() + "%");
        }
        if (request.role() != null && !request.role().isBlank()) {
            query.setParameter("role", request.role().trim().toUpperCase());
        }
        if (request.active() != null) {
            query.setParameter("active", request.active());
        }
        if (request.studentStatus() != null && !request.studentStatus().isBlank()) {
            query.setParameter("studentStatus", request.studentStatus().trim().toUpperCase());
        }
        if (request.departmentId() != null) {
            query.setParameter("departmentId", request.departmentId());
        }
    }

    private String buildWhereClause(ManageUsersQuery query) {
        List<String> clauses = new ArrayList<>();

        if (query.role() != null && !query.role().isBlank()) {
            clauses.add("u.role = :role");
        }
        if (query.keyword() != null && !query.keyword().isBlank()) {
            clauses.add("""
                    (
                        LOWER(COALESCE(s.name, l.name, a.name, '')) LIKE :keyword
                        OR LOWER(COALESCE(u.email, '')) LIKE :keyword
                        OR LOWER(COALESCE(s.student_code, '')) LIKE :keyword
                        OR LOWER(COALESCE(l.lecturer_code, '')) LIKE :keyword
                    )
                    """);
        }
        if (query.active() != null) {
            clauses.add("u.verify = :active");
        }
        if (query.studentStatus() != null && !query.studentStatus().isBlank()) {
            clauses.add("s.status = :studentStatus");
        }
        if (query.departmentId() != null) {
            clauses.add("d.dept_id = :departmentId");
        }

        if (clauses.isEmpty()) {
            return "";
        }

        return " WHERE " + String.join(" AND ", clauses);
    }

    private String buildOrderClause(String sortBy, String sortDir) {
        String normalizedSortBy = sortBy == null ? "name" : sortBy.trim().toLowerCase();
        String direction = "desc".equalsIgnoreCase(sortDir) ? "DESC" : "ASC";

        String expression = switch (normalizedSortBy) {
            case "email" -> "u.email";
            case "role" -> "u.role";
            case "department" -> "d.name";
            case "status" -> "s.status";
            case "active" -> "u.verify";
            default -> "COALESCE(s.name, l.name, a.name)";
        };

        return " ORDER BY " + expression + " " + direction + ", u.user_id ASC";
    }

    private long getLong(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof BigInteger bigInteger) {
            return bigInteger.longValue();
        }
        return ((Number) value).longValue();
    }

}
