package com.ttcs.backend.adapter.out.persistence.teacher;

import com.ttcs.backend.application.domain.model.Teacher;
import com.ttcs.backend.application.port.out.LoadTeacherByUserIdPort;
import com.ttcs.backend.common.PersistenceAdapter;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@PersistenceAdapter
@RequiredArgsConstructor
public class TeacherPersistenceAdapter implements LoadTeacherByUserIdPort {

    private final TeacherRepository teacherRepository;

    @Override
    public Optional<Teacher> loadByUserId(Integer userId) {
        return teacherRepository.findById(userId).map(TeacherMapper::toDomain);
    }
}
