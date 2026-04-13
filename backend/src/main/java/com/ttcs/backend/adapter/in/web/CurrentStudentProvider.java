package com.ttcs.backend.adapter.in.web;

import com.ttcs.backend.application.domain.model.Status;
import com.ttcs.backend.application.domain.model.Student;
import com.ttcs.backend.application.port.out.auth.LoadStudentByIdPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Component
@RequiredArgsConstructor
public class CurrentStudentProvider {

    private final LoadStudentByIdPort loadStudentByIdPort;

    public Integer currentStudentId() {
        return currentUserId();
    }

    public Integer currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new ResponseStatusException(UNAUTHORIZED, "Unauthorized");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof Integer userId) {
            return userId;
        }

        throw new ResponseStatusException(FORBIDDEN, "Authenticated student identity is unavailable");
    }

    public void ensureActiveStudentAccount() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || authentication instanceof AnonymousAuthenticationToken
                || !authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_STUDENT"))) {
            return;
        }

        Integer userId = currentUserId();
        Student student = loadStudentByIdPort.loadByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(FORBIDDEN, "Student profile not found"));

        if (student.getStatus() != Status.ACTIVE) {
            throw new ResponseStatusException(FORBIDDEN, "Student account is not active");
        }
    }
}
