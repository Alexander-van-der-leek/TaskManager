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


    public UserDTO createUser(UserDTO userDTO, UUID userId) {

        Role role = roleRepository.findById(userDTO.getRole().getId())
                .orElseThrow(() -> new RuntimeException("Role not found"));

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setRole(role);
        user.setEmail(userDTO.getEmail());
        user.setName(userDTO.getName());
        user.setGoogleId(userDTO.getGoogleId());
        user.setIsActive(true);
        User savedUser = userRepository.save(user);
        return convertToDTO(savedUser);
    }

    public UserDTO updateUser(UserDTO userDTO, UUID userId) {
        User user = userRepository.findById(userDTO.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

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


    public void deactivateUser(UUID id, UUID userId) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setIsActive(false);

        userRepository.save(user);
    }


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
}
