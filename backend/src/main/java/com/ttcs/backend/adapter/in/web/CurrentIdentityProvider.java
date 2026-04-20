package com.ttcs.backend.adapter.in.web;

import com.ttcs.backend.application.domain.model.Role;
import com.ttcs.backend.application.domain.model.Status;
import com.ttcs.backend.application.domain.model.Student;
import com.ttcs.backend.application.port.out.auth.LoadStudentByIdPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Component
@RequiredArgsConstructor
public class CurrentIdentityProvider {

    private final LoadStudentByIdPort loadStudentByIdPort;

    public Integer currentUserId() {
        Authentication authentication = currentAuthentication();

        Object principal = authentication.getPrincipal();
        if (principal instanceof Integer userId) {
            return userId;
        }

        throw new ResponseStatusException(FORBIDDEN, "Authenticated user identity is unavailable");
    }

    public Integer currentStudentProfileId() {
        return currentStudentProfile().getId();
    }

    public Role currentRole() {
        Authentication authentication = currentAuthentication();

        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            return Role.ADMIN;
        }
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_LECTURER"))) {
            return Role.LECTURER;
        }
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_STUDENT"))) {
            return Role.STUDENT;
        }

        throw new ResponseStatusException(FORBIDDEN, "Authenticated user role is unavailable");
    }

    public void ensureActiveStudentAccount() {
        if (currentRole() != Role.STUDENT) {
            return;
        }

        Student student = currentStudentProfile();
        if (student.getStatus() != Status.ACTIVE
                || student.getUser() == null
                || !Boolean.TRUE.equals(student.getUser().getVerified())) {
            throw new ResponseStatusException(FORBIDDEN, "Student account is not active");
        }
    }

    private Student currentStudentProfile() {
        Integer userId = currentUserId();
        return loadStudentByIdPort.loadByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(FORBIDDEN, "Student profile not found"));
    }

    private Authentication currentAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new ResponseStatusException(UNAUTHORIZED, "Unauthorized");
        }

        return authentication;
    }
}
