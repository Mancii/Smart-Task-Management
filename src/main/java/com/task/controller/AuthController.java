package com.task.controller;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.task.dto.*;
import com.task.exception.InvalidTokenException;
import com.task.service.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
@Tag(name = "Authentication", description = "Authentication and authorization endpoints")
public class AuthController {

private final AuthService authService;
private final TokenService tokenService;
private final JwtUserDetailsService jwtUserDetailsService;
private final VerificationTokenService verificationTokenService;

@PostMapping("/register")
@Operation(
	summary = "Register a new user",
	description = "Creates a new user account and sends a verification email")
@ApiResponses(
	value = {
		@ApiResponse(
			responseCode = "201",
			description = "User registered successfully",
			content =
				@Content(
					mediaType = "application/json",
					schema = @Schema(implementation = BaseResponse.class),
					examples =
						@ExampleObject(
							value =
								"{\"success\":true,\"message\":\"Registration successful. Please"
									+ " check your email to verify your"
									+ " account.\",\"data\":\"\",\"timestamp\":\"2024-01-01T12:00:00Z\"}"))),
		@ApiResponse(
			responseCode = "400",
			description = "Invalid input data",
			content =
				@Content(
					mediaType = "application/json",
					schema = @Schema(implementation = ErrorResponse.class))),
		@ApiResponse(
			responseCode = "409",
			description = "User already exists",
			content =
				@Content(
					mediaType = "application/json",
					schema = @Schema(implementation = ErrorResponse.class)))
	})
public ResponseEntity<BaseResponse<String>> register(
	@Parameter(description = "User registration details", required = true) @RequestBody @Valid
		AuthenticationRequest request) {
	authService.register(request);
	return ResponseEntity.status(HttpStatus.CREATED)
		.body(
			BaseResponse.success(
				"Registration successful. Please check your email to verify your account.", ""));
}

@PostMapping("/authenticate")
@Operation(
	summary = "Authenticate user",
	description = "Authenticates a user and returns JWT tokens")
@ApiResponses(
	value = {
		@ApiResponse(
			responseCode = "200",
			description = "Authentication successful",
			content =
				@Content(
					mediaType = "application/json",
					schema = @Schema(implementation = BaseResponse.class))),
		@ApiResponse(
			responseCode = "401",
			description = "Invalid credentials",
			content =
				@Content(
					mediaType = "application/json",
					schema = @Schema(implementation = ErrorResponse.class))),
		@ApiResponse(
			responseCode = "423",
			description = "Account is locked",
			content =
				@Content(
					mediaType = "application/json",
					schema = @Schema(implementation = ErrorResponse.class)))
	})
public ResponseEntity<BaseResponse<AuthResponse>> authenticate(
	@Parameter(description = "User credentials", required = true) @RequestBody @Valid
		AuthenticationRequest request) {
	AuthResponse authResponse = authService.login(request);
	return ResponseEntity.ok(BaseResponse.success("Authentication successful", authResponse));
}

@PostMapping("/refreshToken")
@Operation(
	summary = "Refresh JWT token",
	description = "Generates new access token using refresh token")
@ApiResponses(
	value = {
		@ApiResponse(
			responseCode = "200",
			description = "Token refreshed successfully",
			content =
				@Content(
					mediaType = "application/json",
					schema = @Schema(implementation = BaseResponse.class))),
		@ApiResponse(
			responseCode = "401",
			description = "Invalid or expired refresh token",
			content =
				@Content(
					mediaType = "application/json",
					schema = @Schema(implementation = ErrorResponse.class)))
	})
public ResponseEntity<BaseResponse<AuthResponse>> refreshToken(
	@Parameter(description = "Refresh token request", required = true) @RequestBody
		JwtRefreshRequest jwtRefreshRequest) {
	AuthResponse response =
		tokenService.getUserNameFromTokenUsingRefreshToken(jwtRefreshRequest.getRefreshToken());
	return ResponseEntity.ok(BaseResponse.success("Token refreshed successfully", response));
}

@PostMapping("/resetPassword")
public ResponseEntity<BaseResponse<String>> resetPassword(
	@RequestBody @Valid ResetPasswordForm passwordForm) {
	jwtUserDetailsService.resetUserPassword(passwordForm);
	return ResponseEntity.ok(
		BaseResponse.success(
			"Password reset successful. You can now log in with your new password.", ""));
}

@PostMapping("/logout")
@Operation(summary = "Logout user", description = "Invalidates the current JWT token")
@SecurityRequirement(name = "bearerAuth")
@ApiResponses(
	value = {
		@ApiResponse(
			responseCode = "200",
			description = "Successfully logged out",
			content =
				@Content(
					mediaType = "application/json",
					schema = @Schema(implementation = BaseResponse.class))),
		@ApiResponse(
			responseCode = "498",
			description = "Logout failed",
			content =
				@Content(
					mediaType = "application/json",
					schema = @Schema(implementation = BaseResponse.class)))
	})
public ResponseEntity<BaseResponse<String>> logout(
	@Parameter(description = "Authorization header with Bearer token", required = true)
		@RequestHeader
		String authorization) {
	String token = authorization.substring(7);
	try {
	tokenService.logout(token);
	return ResponseEntity.ok(BaseResponse.success("Successfully logged out", ""));
	} catch (Exception e) {
	return ResponseEntity.status(498)
		.body(BaseResponse.error("Logout failed", "AUTH_LOGOUT_FAILED", e.getMessage()));
	}
}

@PostMapping("/kill")
public ResponseEntity<BaseResponse<String>> killSession(@RequestParam long id) {
	try {
	tokenService.kill(id);
	return ResponseEntity.ok(BaseResponse.success("Session terminated successfully", ""));
	} catch (Exception e) {
	return ResponseEntity.status(498)
		.body(
			BaseResponse.error(
				"Failed to terminate session", "SESSION_TERMINATION_FAILED", e.getMessage()));
	}
}

@GetMapping("/verify-email")
@Operation(
	summary = "Verify email address",
	description = "Verifies user's email address using verification token")
@ApiResponses(
	value = {
		@ApiResponse(
			responseCode = "200",
			description = "Email verified successfully",
			content =
				@Content(
					mediaType = "application/json",
					schema = @Schema(implementation = BaseResponse.class))),
		@ApiResponse(
			responseCode = "400",
			description = "Invalid or expired verification token",
			content =
				@Content(
					mediaType = "application/json",
					schema = @Schema(implementation = BaseResponse.class))),
		@ApiResponse(
			responseCode = "500",
			description = "Internal server error during verification",
			content =
				@Content(
					mediaType = "application/json",
					schema = @Schema(implementation = BaseResponse.class)))
	})
public ResponseEntity<BaseResponse<String>> verifyEmail(
	@Parameter(description = "Email verification token", required = true) @RequestParam
		String token) {
	try {
	verificationTokenService.verifyEmailToken(token);
	return ResponseEntity.ok(
		BaseResponse.success("Email verified successfully. You can now log in.", ""));
	} catch (InvalidTokenException e) {
	return ResponseEntity.status(HttpStatus.BAD_REQUEST)
		.body(
			BaseResponse.error(
				e.getMessage(),
				"INVALID_VERIFICATION_TOKEN",
				"The verification token is invalid or has expired."));
	} catch (Exception e) {
	log.error("Email verification failed", e);
	return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
		.body(
			BaseResponse.error(
				"An error occurred while verifying your email. Please try again.",
				"VERIFICATION_FAILED",
				e.getMessage()));
	}
}
}
