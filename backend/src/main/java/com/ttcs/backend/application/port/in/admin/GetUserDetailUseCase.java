package com.ttcs.backend.application.port.in.admin;

public interface GetUserDetailUseCase {
    ManagedUserDetailResult getUserDetail(Integer userId);
}
