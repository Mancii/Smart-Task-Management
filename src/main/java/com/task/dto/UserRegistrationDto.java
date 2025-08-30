package com.task.dto;

import com.task.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRegistrationDto {
    private String username;
    private String password;
    private String email;
    private UserRole role;
    private Long statusId;
    private String mobileNumber;
    private Date passwordExpiryDate;
    private boolean enabled;
}
