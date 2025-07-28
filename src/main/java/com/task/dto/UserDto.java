package com.task.dto;

import com.task.entity.UserRole;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDto {
    private String username;
    private String password;
    private String email;
    private UserRole role;
}
