package com.taskmanagement.service;

import com.taskmanagement.dto.UserDTO;
import com.taskmanagement.model.Role;
import com.taskmanagement.model.User;
import com.taskmanagement.repository.UserRepository;
import com.taskmanagement.repository.RoleRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }


    public List<UserDTO> getAllUsers(UUID userId) {
        return userRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }


    public UserDTO getUserById(UUID id, UUID userId) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return convertToDTO(user);
    }

    // Create a new user
    public UserDTO createUser(UserDTO userDTO, UUID userId) {
        // Ensure role exists before saving the user
        Role role = roleRepository.findById(userDTO.getRole().getId())
                .orElseThrow(() -> new RuntimeException("Role not found"));

        User user = new User();
        user.setId(UUID.randomUUID());  // Setting a new UUID
        user.setRole(role);
        user.setEmail(userDTO.getEmail());
        user.setName(userDTO.getName());
        user.setGoogleId(userDTO.getGoogleId());

        User savedUser = userRepository.save(user);
        return convertToDTO(savedUser);
    }

    // Update an existing user
    public UserDTO updateUser(UserDTO userDTO, UUID userId) {
        User user = userRepository.findById(userDTO.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update user fields based on userDTO
        if (userDTO.getRole() != null) {
            Role role = roleRepository.findById(userDTO.getRole().getId())
                    .orElseThrow(() -> new RuntimeException("Role not found"));
            user.setRole(role);
        }

        user.setName(userDTO.getName());
        user.setEmail(userDTO.getEmail());
        user.setGoogleId(userDTO.getGoogleId());

        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }

    // Delete a user
    public void deleteUser(UUID id, UUID userId) {
        userRepository.deleteById(id);
    }

    // Convert User entity to UserDTO
    private UserDTO convertToDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getRole(),
                user.getEmail(),
                user.getName(),
                user.getGoogleId(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    // Convert UserDTO to User entity
    private User convertToEntity(UserDTO userDTO) {
        User user = new User();
        user.setId(userDTO.getId());
        user.setRole(userDTO.getRole());
        user.setEmail(userDTO.getEmail());
        user.setName(userDTO.getName());
        user.setGoogleId(userDTO.getGoogleId());
        user.setCreatedAt(userDTO.getCreatedAt());
        user.setUpdatedAt(userDTO.getUpdatedAt());
        return user;
    }
}
