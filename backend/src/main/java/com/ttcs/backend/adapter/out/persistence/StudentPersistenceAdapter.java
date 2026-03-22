package com.ttcs.backend.adapter.out.persistence;

import com.ttcs.backend.application.domain.model.Student;
import com.ttcs.backend.application.port.out.LoadStudentPort;
import com.ttcs.backend.common.PersistenceAdapter;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@PersistenceAdapter
@RequiredArgsConstructor
public class StudentPersistenceAdapter implements LoadStudentPort {
    private final StudentRepository studentRepository;

    @Override
    public Optional<Student> loadById(Integer studentId) {
        return studentRepository.findById(studentId)
                .map(StudentMapper::toDomain);
    }
}
