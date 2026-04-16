package com.ttcs.backend.application.port.in.admin;

public interface GetUsersUseCase {
    ManagedUserPageResult getUsers(GetUsersQuery query);
}
