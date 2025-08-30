package com.task.controller;

import com.task.dto.BaseResponse;
import com.task.dto.UserDto;
import com.task.dto.UserRegistrationDto;
import com.task.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "APIs for user management")
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users with pagination")
    public ResponseEntity<BaseResponse<List<UserDto>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String[] sort) {
        
        String sortField = sort[0];
        String sortDirection = sort.length > 1 ? sort[1] : "asc";
        
        Pageable pageable = PageRequest.of(
            page, 
            size, 
            Sort.by(Sort.Direction.fromString(sortDirection), sortField)
        );
        
        Page<UserDto> usersPage = userService.getAllUsers(pageable);
        
        BaseResponse<List<UserDto>> response = new BaseResponse<>();
        response.setSuccess(true);
        response.setMessage("Users retrieved successfully");
        response.setData(usersPage.getContent());
        response.setMeta(new BaseResponse.Meta(
            page,
            size,
            usersPage.getTotalElements(),
            usersPage.getTotalPages()
        ));
        
        return ResponseEntity.ok(response);
    }
}
