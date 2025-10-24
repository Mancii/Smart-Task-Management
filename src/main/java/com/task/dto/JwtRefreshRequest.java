package com.task.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtRefreshRequest implements Serializable {

	private static final long serialVersionUID = 1898736269687613244L;

	@NotBlank(message = "Refresh token is required")
	private String refreshToken;
}
