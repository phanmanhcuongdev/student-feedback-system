package com.ttcs.backend.config;

import com.ttcs.backend.adapter.out.persistence.*;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;

    @Override
    public UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        UserEntity userEntity = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy user với email: " + username));

        StatusEntity studentStatus = null;
        if (userEntity.getRole() == RoleEntity.STUDENT) {
            StudentEntity studentEntity = studentRepository.findByUserId(userEntity.getId())
                    .orElseThrow(() -> new UsernameNotFoundException(
                            "Khong tim thay thong tin student cho userId: " + userEntity.getId()
                    ));
            studentStatus = studentEntity.getStatus();
        }


        return new CustomerUserDetails(userEntity, studentStatus);
    }
}
