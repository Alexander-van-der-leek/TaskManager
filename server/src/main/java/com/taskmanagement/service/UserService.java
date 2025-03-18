package com.taskmanagement.service;

import com.taskmanagement.dto.UserDTO;
import com.taskmanagement.exception.ResourceNotFound;
import com.taskmanagement.exception.UserNotFoundException;
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
    public List<UserDTO> searchUsersByName(String name) {
        logger.debug("Searching users with name containing: {}", name);
        return userRepository.findByNameContainingIgnoreCase(name).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserDTO updateUser( UserDTO userDTO, String name,UUID id) {
        logger.info("Updating user with ID: {}", id);

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFound("User not found with ID: " + id));

        if (userDTO.getName() != null) {
            existingUser.setName(userDTO.getName());
        }
        if (userDTO.getEmail() != null) {
            existingUser.setEmail(userDTO.getEmail());
        }
        if (userDTO.getRole() != null) {
            existingUser.setRole(userDTO.getRole());
        }
        if (userDTO.getIsActive() != null) {
            existingUser.setIsActive(userDTO.getIsActive());
        }

        User updatedUser = userRepository.save(existingUser);

        return convertToDTO(updatedUser);
    }


    @Transactional
    public boolean deactivateUser(String name) {
        List<User> users = userRepository.findByNameContainingIgnoreCase(name);

        if (users.isEmpty()) {
            logger.error("User not found with name: {}", name);
            throw new UserNotFoundException(UUID.randomUUID());
        }

        User userToDeactivate = users.get(0);
        userToDeactivate.setIsActive(false);
        userRepository.save(userToDeactivate);

        logger.info("User with name: {} has been deactivated.", name);
        return true;
    }

    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setGoogleId(user.getGoogleId());
        dto.setIsActive(user.getIsActive());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }
}
