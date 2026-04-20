package com.ttcs.backend.adapter.out.persistence.student;

import com.ttcs.backend.application.domain.model.Student;
import com.ttcs.backend.application.port.out.LoadSurveyRecipientCandidatePort;
import com.ttcs.backend.application.port.out.LoadStudentPort;
import com.ttcs.backend.common.PersistenceAdapter;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@PersistenceAdapter
@RequiredArgsConstructor
public class StudentPersistenceAdapter implements LoadStudentPort, LoadSurveyRecipientCandidatePort {
    private final StudentRepository studentRepository;

    @Override
    public Optional<Student> loadById(Integer studentId) {
        return studentRepository.findById(studentId)
                .map(StudentMapper::toDomain);
    }

    @Override
    public List<Student> loadByIds(List<Integer> studentIds) {
        if (studentIds == null || studentIds.isEmpty()) {
            return List.of();
        }
        List<Integer> orderedIds = new LinkedHashSet<>(studentIds).stream()
                .filter(Objects::nonNull)
                .toList();
        Map<Integer, Student> studentsById = studentRepository.findAllById(orderedIds).stream()
                .map(StudentMapper::toDomain)
                .collect(Collectors.toMap(Student::getId, Function.identity()));
        return orderedIds.stream()
                .map(studentsById::get)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public List<Student> loadActiveStudents() {
        return studentRepository.findByStatusAndUser_VerifiedTrueOrderByIdAsc(com.ttcs.backend.adapter.out.persistence.StatusEntity.ACTIVE).stream()
                .map(StudentMapper::toDomain)
                .toList();
    }

    @Override
    public List<Student> loadActiveStudentsByDepartment(Integer departmentId) {
        return studentRepository.findByStatusAndDepartment_IdAndUser_VerifiedTrueOrderByIdAsc(com.ttcs.backend.adapter.out.persistence.StatusEntity.ACTIVE, departmentId).stream()
                .map(StudentMapper::toDomain)
                .toList();
    }
}
