package com.task.service;

import com.task.dto.UserDto;
import com.task.entity.User;
import com.task.entity.UserRole;
import com.task.repo.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private UserService userService;

    private User user1;
    private User user2;
    private UserDto userDto1;
    private UserDto userDto2;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setId(1L);
        user1.setUsername("user1");
        user1.setEmail("user1@example.com");
        user1.setRole(UserRole.USER);

        user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
        user2.setRole(UserRole.ADMIN);

        userDto1 = new UserDto();
        userDto1.setId(1L);
        userDto1.setUsername("user1");
        userDto1.setEmail("user1@example.com");

        userDto2 = new UserDto();
        userDto2.setId(2L);
        userDto2.setUsername("user2");
        userDto2.setEmail("user2@example.com");

        pageable = PageRequest.of(0, 10);
    }

    @Test
    void getAllUsers_ShouldReturnPagedUserDtos_WhenUsersExist() {
        // Given
        List<User> users = List.of(user1, user2);
        Page<User> userPage = new PageImpl<>(users, pageable, users.size());
        
        when(userRepository.findAll(pageable)).thenReturn(userPage);
        when(modelMapper.map(user1, UserDto.class)).thenReturn(userDto1);
        when(modelMapper.map(user2, UserDto.class)).thenReturn(userDto2);

        // When
        Page<UserDto> result = userService.getAllUsers(pageable);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        
        UserDto firstDto = result.getContent().get(0);
        assertEquals(userDto1.getId(), firstDto.getId());
        assertEquals(userDto1.getUsername(), firstDto.getUsername());
        assertEquals(userDto1.getEmail(), firstDto.getEmail());

        UserDto secondDto = result.getContent().get(1);
        assertEquals(userDto2.getId(), secondDto.getId());
        assertEquals(userDto2.getUsername(), secondDto.getUsername());
        assertEquals(userDto2.getEmail(), secondDto.getEmail());

        verify(userRepository).findAll(pageable);
        verify(modelMapper).map(user1, UserDto.class);
        verify(modelMapper).map(user2, UserDto.class);
    }

    @Test
    void getAllUsers_ShouldReturnEmptyPage_WhenNoUsersExist() {
        // Given
        Page<User> emptyPage = new PageImpl<>(List.of(), pageable, 0);
        when(userRepository.findAll(pageable)).thenReturn(emptyPage);

        // When
        Page<UserDto> result = userService.getAllUsers(pageable);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());

        verify(userRepository).findAll(pageable);
        verify(modelMapper, never()).map(any(User.class), eq(UserDto.class));
    }

    @Test
    void getAllUsers_ShouldHandleDifferentPageSizes() {
        // Given
        Pageable smallPageable = PageRequest.of(0, 1);
        List<User> users = List.of(user1);
        Page<User> userPage = new PageImpl<>(users, smallPageable, 2); // Total 2, but only 1 per page
        
        when(userRepository.findAll(smallPageable)).thenReturn(userPage);
        when(modelMapper.map(user1, UserDto.class)).thenReturn(userDto1);

        // When
        Page<UserDto> result = userService.getAllUsers(smallPageable);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getTotalElements()); // Total elements
        assertEquals(1, result.getContent().size()); // Current page size
        assertEquals(2, result.getTotalPages()); // Should have 2 pages

        verify(userRepository).findAll(smallPageable);
        verify(modelMapper).map(user1, UserDto.class);
    }

    @Test
    void getAllUsers_ShouldHandleSecondPage() {
        // Given
        Pageable secondPageable = PageRequest.of(1, 1);
        List<User> users = List.of(user2);
        Page<User> userPage = new PageImpl<>(users, secondPageable, 2);
        
        when(userRepository.findAll(secondPageable)).thenReturn(userPage);
        when(modelMapper.map(user2, UserDto.class)).thenReturn(userDto2);

        // When
        Page<UserDto> result = userService.getAllUsers(secondPageable);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals(1, result.getNumber()); // Current page number

        UserDto dto = result.getContent().get(0);
        assertEquals(userDto2.getId(), dto.getId());

        verify(userRepository).findAll(secondPageable);
        verify(modelMapper).map(user2, UserDto.class);
    }
}