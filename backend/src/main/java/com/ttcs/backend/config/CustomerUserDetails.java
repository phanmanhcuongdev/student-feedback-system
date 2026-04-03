package com.ttcs.backend.config;

import com.ttcs.backend.adapter.out.persistence.RoleEntity;
import com.ttcs.backend.adapter.out.persistence.StatusEntity;
import com.ttcs.backend.adapter.out.persistence.UserEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomerUserDetails implements UserDetails {
    private final UserEntity userEntity;
    private final RoleEntity role;
    private final StatusEntity studentStatus;

    public CustomerUserDetails(UserEntity userEntity, StatusEntity studentStatus) {
        this.userEntity = userEntity;
        this.role = userEntity.getRole();
        this.studentStatus = studentStatus;
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return userEntity.getPassword();
    }

    @Override
    public String getUsername() {
        return userEntity.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        if (role != RoleEntity.STUDENT){
            return true;
        } else {
            return studentStatus == StatusEntity.ACTIVE;
        }
    }
}
