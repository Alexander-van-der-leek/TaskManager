package com.taskmanagement.controller;

import com.taskmanagement.dto.UserDTO;
import com.taskmanagement.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthoritiesContainer;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;


import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserControllerTest {

    @InjectMocks
    private UserController userController;

    @Mock
    private UserService userService;

    @Mock
    private UserDetails userDetails;

    @Mock
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllUsers() {
        UUID userId = UUID.randomUUID();
        when(userDetails.getUsername()).thenReturn(userId.toString());

        UserDTO userDTO = new UserDTO();
        userDTO.setName("noluthandoh14");
        userDTO.setEmail("noluthandoh14@gmail.com");
        when(userService.getAllUsers()).thenReturn(Collections.singletonList(userDTO));

        ResponseEntity<List<UserDTO>> response = userController.getAllUsers(userDetails);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
        assertEquals("noluthandoh14", response.getBody().get(0).getName());
        assertEquals("noluthandoh14@gmail.com", response.getBody().get(0).getEmail());
    }

    @Test
    void testSearchUsersByName() {
        String name = "noluthandoh14";
        UUID userId = UUID.randomUUID();
        when(userDetails.getUsername()).thenReturn(userId.toString());

        UserDTO userDTO = new UserDTO();
        userDTO.setName(name);
        userDTO.setEmail("noluthandoh14@gmail.com");
        when(userService.searchUsersByName(name)).thenReturn(Collections.singletonList(userDTO));

        ResponseEntity<List<UserDTO>> response = userController.searchUsersByName(name, userDetails);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
        assertEquals("noluthandoh14", response.getBody().get(0).getName());
        assertEquals("noluthandoh14@gmail.com", response.getBody().get(0).getEmail());
    }

    @Test
    @WithMockUser(username = "noluthandoh14", roles = {"DEVELOPER"})
    void testUpdateUser_FailedAuthorization() {
        // Simulate a non-admin user with "DEVELOPER" role
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "noluthandoh14", // username
                null, // password
                Collections.singletonList(new SimpleGrantedAuthority("DEVELOPER")) // authorities
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Create a userDTO object
        UserDTO userDTO = new UserDTO();
        userDTO.setName("Updated Name");

        // Simulate the update user request
        ResponseEntity<UserDTO> response = userController.updateUser("noluthandoh14", userDTO, userDetails);

        // Assertions for the failed authorization
        assertEquals(403, response.getStatusCodeValue());
        assertEquals("Error: Insufficient Permissions", response.getBody().getName());
    }

    @Test
    void testUpdateUser_FailedAuthorizationManually() {
        // Manually create an Authentication object with the "DEVELOPER" role
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "noluthandoh14",
                null,
                Collections.singletonList(new SimpleGrantedAuthority("DEVELOPER")) // authorities
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Create a userDTO object
        UserDTO userDTO = new UserDTO();
        userDTO.setName("Updated Name");

        // Simulate the update user request
        ResponseEntity<UserDTO> response = userController.updateUser("noluthandoh14", userDTO, userDetails);

        // Assertions for the failed authorization
        assertEquals(403, response.getStatusCodeValue());
        assertEquals("Error: Insufficient Permissions", response.getBody().getName());
    }

    @Test
    @WithMockUser(username = "noluthandoh14", roles = {"DEVELOPER"})
    void testDeactivateUser_FailedAuthorization() {
        String name = "noluthandoh14";

        // Simulate the deactivate user request with a non-admin user
        ResponseEntity<String> response = userController.deactivateUser(name, userDetails);

        // Assertions for failed authorization
        assertEquals(403, response.getStatusCodeValue());
        assertEquals("You are not authorized to deactivate users.", response.getBody());
    }
}
