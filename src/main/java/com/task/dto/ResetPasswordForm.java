package com.task.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Form for resetting a user's password.
 * Either email or username must be provided along with the new password.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordForm implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private static final String PASSWORD_PATTERN = 
        "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&$^()–]).{8,100}$";
    
    private String email;
    private String userName;
    
    @NotBlank(message = "New password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Pattern(
        regexp = PASSWORD_PATTERN,
        message = "Password must contain at least one digit, one lowercase, " +
                 "one uppercase letter and one special character"
    )
    private String newPassword;
    
    @AssertTrue(message = "Either email or username must be provided")
    private boolean isEmailOrUsernameProvided() {
        return isNotEmpty(email) || isNotEmpty(userName);
    }
    
    private boolean isNotEmpty(String value) {
        return value != null && !value.isBlank();
    }
}
