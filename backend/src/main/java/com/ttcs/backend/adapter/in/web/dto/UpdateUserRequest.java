package com.ttcs.backend.adapter.in.web.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserRequest {
    private String email;
    private String name;
    private Integer departmentId;
    private String studentCode;
    private String teacherCode;
}
