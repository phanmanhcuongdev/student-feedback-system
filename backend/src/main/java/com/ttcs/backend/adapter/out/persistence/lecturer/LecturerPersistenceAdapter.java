package com.ttcs.backend.adapter.out.persistence.lecturer;

import com.ttcs.backend.application.domain.model.Lecturer;
import com.ttcs.backend.application.port.out.LoadLecturerByUserIdPort;
import com.ttcs.backend.common.PersistenceAdapter;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@PersistenceAdapter
@RequiredArgsConstructor
public class LecturerPersistenceAdapter implements LoadLecturerByUserIdPort {

    private final LecturerRepository lecturerRepository;

    @Override
    public Optional<Lecturer> loadByUserId(Integer userId) {
        return lecturerRepository.findById(userId).map(LecturerMapper::toDomain);
    }
}
