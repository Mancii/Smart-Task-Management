package com.task.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.task.dto.BaseResponse;
import com.task.dto.ErrorResponse;
import com.task.dto.UserDto;
import com.task.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "APIs for user management")
public class UserController {

private final UserService userService;

@GetMapping
@PreAuthorize("hasRole('ADMIN')")
@Operation(
	summary = "Get all users with pagination",
	description = "Retrieves a paginated list of all users. Requires ADMIN role.")
@SecurityRequirement(name = "bearerAuth")
@ApiResponses(
	value = {
		@ApiResponse(
			responseCode = "200",
			description = "Users retrieved successfully",
			content =
				@Content(
					mediaType = "application/json",
					schema = @Schema(implementation = BaseResponse.class))),
		@ApiResponse(
			responseCode = "401",
			description = "Unauthorized - Invalid or missing token",
			content =
				@Content(
					mediaType = "application/json",
					schema = @Schema(implementation = ErrorResponse.class))),
		@ApiResponse(
			responseCode = "403",
			description = "Forbidden - Insufficient privileges",
			content =
				@Content(
					mediaType = "application/json",
					schema = @Schema(implementation = ErrorResponse.class)))
	})
public ResponseEntity<BaseResponse<List<UserDto>>> getAllUsers(
	@Parameter(description = "Page number (0-based)", example = "0")
		@RequestParam(defaultValue = "0")
		int page,
	@Parameter(description = "Number of items per page", example = "10")
		@RequestParam(defaultValue = "10")
		int size,
	@Parameter(description = "Sort criteria in format: property,direction", example = "id,asc")
		@RequestParam(defaultValue = "id,asc")
		String[] sort) {

	String sortField = sort[0];
	String sortDirection = sort.length > 1 ? sort[1] : "asc";

	Pageable pageable =
		PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection), sortField));

	Page<UserDto> usersPage = userService.getAllUsers(pageable);

	BaseResponse<List<UserDto>> response = new BaseResponse<>();
	response.setSuccess(true);
	response.setMessage("Users retrieved successfully");
	response.setData(usersPage.getContent());
	response.setMeta(
		new BaseResponse.Meta(page, size, usersPage.getTotalElements(), usersPage.getTotalPages()));

	return ResponseEntity.ok(response);
}
}
