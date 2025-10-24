package com.task.controller;

import com.task.dto.BaseResponse;
import com.task.dto.UserDto;
import com.task.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    private List<UserDto> userDtos;
    private Page<UserDto> userPage;

    @BeforeEach
    void setUp() {
        UserDto user1 = new UserDto();
        user1.setId(1L);
        user1.setUsername("user1");
        user1.setEmail("user1@example.com");

        UserDto user2 = new UserDto();
        user2.setId(2L);
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");

        userDtos = Arrays.asList(user1, user2);
        userPage = new PageImpl<>(userDtos, PageRequest.of(0, 10), userDtos.size());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_ShouldReturnOk_WhenAdminUser() throws Exception {
        // Given
        when(userService.getAllUsers(any(Pageable.class))).thenReturn(userPage);

        // When & Then
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Users retrieved successfully"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].username").value("user1"))
                .andExpect(jsonPath("$.data[0].email").value("user1@example.com"))
                .andExpect(jsonPath("$.data[1].id").value(2))
                .andExpect(jsonPath("$.data[1].username").value("user2"))
                .andExpect(jsonPath("$.data[1].email").value("user2@example.com"))
                .andExpect(jsonPath("$.meta.page").value(0))
                .andExpect(jsonPath("$.meta.size").value(10))
                .andExpect(jsonPath("$.meta.totalElements").value(2))
                .andExpect(jsonPath("$.meta.totalPages").value(1));

        verify(userService).getAllUsers(any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllUsers_ShouldReturnForbidden_WhenRegularUser() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());

        verify(userService, never()).getAllUsers(any(Pageable.class));
    }

    @Test
    void getAllUsers_ShouldReturnUnauthorized_WhenUnauthenticated() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());

        verify(userService, never()).getAllUsers(any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_ShouldHandlePagination_WhenCustomParameters() throws Exception {
        // Given
        Page<UserDto> customPage = new PageImpl<>(
            Arrays.asList(userDtos.get(0)), 
            PageRequest.of(1, 1), 
            2
        );
        when(userService.getAllUsers(any(Pageable.class))).thenReturn(customPage);

        // When & Then
        mockMvc.perform(get("/api/users")
                .param("page", "1")
                .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.meta.page").value(1))
                .andExpect(jsonPath("$.meta.size").value(1))
                .andExpect(jsonPath("$.meta.totalElements").value(2))
                .andExpect(jsonPath("$.meta.totalPages").value(2));

        verify(userService).getAllUsers(any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_ShouldHandleSorting_WhenSortParametersProvided() throws Exception {
        // Given
        when(userService.getAllUsers(any(Pageable.class))).thenReturn(userPage);

        // When & Then
        mockMvc.perform(get("/api/users")
                .param("sort", "username,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Verify that the service was called with the correct sort parameters
        verify(userService).getAllUsers(argThat(pageable -> {
            Sort sort = pageable.getSort();
            Sort.Order order = sort.getOrderFor("username");
            return order != null && order.getDirection() == Sort.Direction.DESC;
        }));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_ShouldUseDefaultSort_WhenNoSortProvided() throws Exception {
        // Given
        when(userService.getAllUsers(any(Pageable.class))).thenReturn(userPage);

        // When & Then
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk());

        // Verify that the service was called with default sort (id, asc)
        verify(userService).getAllUsers(argThat(pageable -> {
            Sort sort = pageable.getSort();
            Sort.Order order = sort.getOrderFor("id");
            return order != null && order.getDirection() == Sort.Direction.ASC;
        }));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_ShouldUseAscendingSort_WhenOnlyFieldProvided() throws Exception {
        // Given
        when(userService.getAllUsers(any(Pageable.class))).thenReturn(userPage);

        // When & Then
        mockMvc.perform(get("/api/users")
                .param("sort", "email"))
                .andExpect(status().isOk());

        // Verify that the service was called with ascending sort by default
        verify(userService).getAllUsers(argThat(pageable -> {
            Sort sort = pageable.getSort();
            Sort.Order order = sort.getOrderFor("email");
            return order != null && order.getDirection() == Sort.Direction.ASC;
        }));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_ShouldReturnEmptyList_WhenNoUsers() throws Exception {
        // Given
        Page<UserDto> emptyPage = new PageImpl<>(Arrays.asList(), PageRequest.of(0, 10), 0);
        when(userService.getAllUsers(any(Pageable.class))).thenReturn(emptyPage);

        // When & Then
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Users retrieved successfully"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0))
                .andExpect(jsonPath("$.meta.totalElements").value(0))
                .andExpect(jsonPath("$.meta.totalPages").value(0));

        verify(userService).getAllUsers(any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_ShouldHandleInvalidPageParameters() throws Exception {
        // Given
        when(userService.getAllUsers(any(Pageable.class))).thenReturn(userPage);

        // When & Then - Test with negative page number (should be handled by Spring)
        mockMvc.perform(get("/api/users")
                .param("page", "-1")
                .param("size", "5"))
                .andExpect(status().isOk()); // Spring Boot handles negative pages gracefully

        verify(userService).getAllUsers(any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_ShouldHandleMultipleSortFields() throws Exception {
        // Given
        when(userService.getAllUsers(any(Pageable.class))).thenReturn(userPage);

        // When & Then
        mockMvc.perform(get("/api/users")
                .param("sort", "username,desc")
                .param("sort", "email,asc"))
                .andExpect(status().isOk());

        // Note: With multiple sort parameters, only the last one is typically used
        // This test verifies the endpoint handles multiple sort parameters gracefully
        verify(userService).getAllUsers(any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_ShouldHandleLargePageSize() throws Exception {
        // Given
        when(userService.getAllUsers(any(Pageable.class))).thenReturn(userPage);

        // When & Then
        mockMvc.perform(get("/api/users")
                .param("page", "0")
                .param("size", "1000"))
                .andExpect(status().isOk());

        verify(userService).getAllUsers(argThat(pageable -> 
            pageable.getPageSize() == 1000
        ));
    }
}