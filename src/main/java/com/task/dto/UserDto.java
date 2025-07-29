package com.task.dto;

import com.task.entity.UserRole;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class UserDto {
    private String username;
    private String password;
    private String email;
    private UserRole role;
    private Long statusId;
    private String mobileNumber;
    private Date passwordExpiryDate;
}
