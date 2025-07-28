package com.task.dto;

import com.task.entity.UserRole;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationRequest {
    private String userName;
    private String password;
    @Email
    private String email;
    private UserRole role;

}
