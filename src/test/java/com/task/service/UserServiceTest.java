package com.task.service;

import com.task.dto.UserDto;
import com.task.entity.User;
import com.task.repo.UserRepository;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Test
    void getAllUsers_mapsEntitiesToDtos() {
        UserRepository userRepository = mock(UserRepository.class);
        ModelMapper modelMapper = new ModelMapper();
        UserService userService = new UserService(userRepository, modelMapper);

        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("john");
        user1.setEmail("john@example.com");

        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("jane");
        user2.setEmail("jane@example.com");

        Page<User> page = new PageImpl<>(List.of(user1, user2));
        when(userRepository.findAll(any(Pageable.class))).thenReturn(page);

        Pageable pageable = PageRequest.of(0, 10);
        Page<UserDto> result = userService.getAllUsers(pageable);

        assertEquals(2, result.getTotalElements());
        List<UserDto> content = result.getContent();
        assertEquals("john", content.get(0).getUsername());
        assertEquals("jane", content.get(1).getUsername());
    }
}
