package com.ttcs.backend.adapter.out.persistence;

import com.ttcs.backend.adapter.out.persistence.user.UserMapper;
import com.ttcs.backend.application.domain.model.User;
import com.ttcs.backend.application.port.out.auth.LoadUserByIdPort;
import com.ttcs.backend.common.PersistenceAdapter;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@PersistenceAdapter
@RequiredArgsConstructor
public class UserPersistenceAdapter implements LoadUserByIdPort {

    private final UserRepository userRepository;

    @Override
    public Optional<User> loadById(Integer userId) {
        return userRepository.findById(userId).map(UserMapper::toDomain);
    }
}
