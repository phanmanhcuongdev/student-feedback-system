package com.ttcs.backend.adapter.in.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {
    private boolean success;
    private String code;
    private Integer userId;
    private String role;
    private String studentStatus;
    private String accessToken;
    private String message;

    public static LoginResponse success(Integer userId, String role, String studentStatus, String accessToken) {
        return new LoginResponse(true, "LOGIN_SUCCESS", userId, role, studentStatus, accessToken, "Dang nhap thanh cong");
    }

    public static LoginResponse fail(String code, String message) {
        return new LoginResponse(false, code, null, null, null, null, message);
    }
}
