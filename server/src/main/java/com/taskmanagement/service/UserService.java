package com.taskmanagement.service;

import com.taskmanagement.dto.UserDTO;
import com.taskmanagement.exception.ResourceNotFound;
import com.taskmanagement.model.User;
import com.taskmanagement.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        logger.debug("Fetching all users");
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserDTO getUserById(UUID id) {
        logger.debug("Fetching user with ID: {}", id);
        return userRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new ResourceNotFound("User not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<UserDTO> searchUsersByName(String name) {
        logger.debug("Searching users with name containing: {}", name);
        return userRepository.findByNameContainingIgnoreCase(name).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRoleName(user.getRole().getName());
        return dto;
    }
}