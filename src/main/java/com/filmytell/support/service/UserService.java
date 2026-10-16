package com.filmytell.support.service;

import com.filmytell.support.dto.auth.UserProfileDto;
import com.filmytell.support.entity.User;
import com.filmytell.support.enums.RoleName;
import com.filmytell.support.exception.ResourceNotFoundException;
import com.filmytell.support.mapper.UserMapper;
import com.filmytell.support.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public List<UserProfileDto> getAllUsers() {
        return userRepository.findAll().stream().map(userMapper::toProfileDto).toList();
    }

    @Transactional(readOnly = true)
    public List<UserProfileDto> getActiveAgents() {
        return userRepository.findAllActiveUsers().stream().map(userMapper::toProfileDto).toList();
    }

    @Transactional(readOnly = true)
    public UserProfileDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return userMapper.toProfileDto(user);
    }

    @Transactional
    public UserProfileDto toggleUserStatus(Long id, boolean active) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        user.setActive(active);
        return userMapper.toProfileDto(userRepository.save(user));
    }
}
