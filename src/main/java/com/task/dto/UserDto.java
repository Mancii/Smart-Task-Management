package com.task.dto;

import com.task.entity.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    @Schema(description = "User ID", example = "1")
    private Long id;

    @NotBlank(message = "Username is required")
    @Schema(description = "User's username", example = "johndoe")
    private String username;

    @Email(message = "Email should be valid")
    @Schema(description = "User's email address", example = "user@example.com")
    private String email;

    @Schema(description = "User's role", example = "USER")
    private UserRole role;

    @Schema(description = "User's mobile number", example = "+1234567890")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid mobile number format")
    private String mobileNumber;

    @Schema(description = "Whether the user account is enabled", example = "true")
    private boolean enabled;

    @Schema(description = "Account status ID")
    private Long statusId;

    @Schema(description = "Password expiry date")
    private Date passwordExpiryDate;
}
