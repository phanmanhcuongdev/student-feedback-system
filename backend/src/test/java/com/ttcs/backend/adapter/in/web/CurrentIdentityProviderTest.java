package com.ttcs.backend.adapter.in.web;

import com.ttcs.backend.application.domain.model.Department;
import com.ttcs.backend.application.domain.model.Role;
import com.ttcs.backend.application.domain.model.Status;
import com.ttcs.backend.application.domain.model.Student;
import com.ttcs.backend.application.domain.model.User;
import com.ttcs.backend.application.port.out.auth.LoadStudentByIdPort;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CurrentIdentityProviderTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldResolveStudentProfileIdFromAuthenticatedUserId() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                6,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_STUDENT"))
        ));
        CurrentIdentityProvider provider = new CurrentIdentityProvider(new StubLoadStudentByIdPort());

        assertEquals(6, provider.currentUserId());
        assertEquals(42, provider.currentStudentProfileId());
    }

    private static final class StubLoadStudentByIdPort implements LoadStudentByIdPort {
        @Override
        public Optional<Student> loadById(Integer studentId) {
            return Optional.of(student(studentId, 6));
        }

        @Override
        public Optional<Student> loadByUserId(Integer userId) {
            return Optional.of(student(42, userId));
        }

        private Student student(Integer id, Integer userId) {
            return new Student(
                    id,
                    new User(userId, "student@example.com", "hashed", Role.STUDENT, true),
                    "Student",
                    "S0006",
                    new Department(1, "Computer Science"),
                    Status.ACTIVE,
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
}
