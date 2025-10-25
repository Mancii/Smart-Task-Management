package com.task.service;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.task.dto.UserDto;
import com.task.repo.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

private final UserRepository userRepository;
private final ModelMapper modelMapper;

public Page<UserDto> getAllUsers(Pageable pageable) {
	return userRepository.findAll(pageable).map(user -> modelMapper.map(user, UserDto.class));
}
}
