package com.taskmanagement.controller;

import com.taskmanagement.dto.UserDTO;
import com.taskmanagement.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers(@AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        logger.info("User {} requesting all users", userId);
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserDTO>> searchUsersByName(
            @RequestParam String name,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        logger.info("User {} searching users by name containing: {}", userId, name);
        return ResponseEntity.ok(userService.searchUsersByName(name));
    }

    @PutMapping("/update")
    public ResponseEntity<UserDTO> updateUser(
            @RequestParam String name,
            @Valid @RequestBody UserDTO userDTO,
            @AuthenticationPrincipal UserDetails userDetails) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // Check if the current user has the required role
        boolean hasAdminRole = authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ADMIN"));

        if (!hasAdminRole) {
            UserDTO errorDTO = new UserDTO();
            errorDTO.setName("Error: Insufficient Permissions");
            errorDTO.setEmail("You do not have permission to update users.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorDTO);
        }
        UUID userId = UUID.fromString(userDetails.getUsername());
        logger.info("User {} updating user with name {}", userId, name);

        UserDTO updatedUser = userService.updateUser(userDTO, name, userId);
        if (updatedUser == null) {
            logger.error("User with name {} not found", name);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/deactivate")
    public ResponseEntity<String> deactivateUser(
            @RequestParam String name,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (!hasRequiredRole(userDetails)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You are not authorized to deactivate users.");
        }

        boolean success = userService.deactivateUser(name);
        if (success) {
            return ResponseEntity.ok("User " + name + " has been deactivated successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No user found with the name: " + name);
        }
    }

//     Helper method to check if the user has either ROLE_ADMIN or ROLE_SUPER_ADMIN role.
    private boolean hasRequiredRole(UserDetails userDetails) {
        return userDetails.getAuthorities().stream()
                .anyMatch(authority -> "ADMIN".equals(authority.getAuthority()));
    }
}
