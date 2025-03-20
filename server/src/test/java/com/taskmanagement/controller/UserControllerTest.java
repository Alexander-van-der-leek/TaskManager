package com.taskmanagement.controller;

import com.taskmanagement.dto.UserDTO;
import com.taskmanagement.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
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

    private UUID testUserId;
    private UUID requesterId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testUserId = UUID.randomUUID();
        requesterId = UUID.randomUUID();
        when(userDetails.getUsername()).thenReturn(requesterId.toString());
    }

    @Test
    void testGetAllUsers() {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(testUserId);
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
        UserDTO userDTO = new UserDTO();
        userDTO.setId(testUserId);
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
    void testUpdateUser_Success() {
        UserDTO userDTO = new UserDTO();
        userDTO.setName("Updated Name");

        UserDTO updatedDTO = new UserDTO();
        updatedDTO.setId(testUserId);
        updatedDTO.setName("Updated Name");
        updatedDTO.setEmail("test@example.com");

        when(userService.updateUser(any(UserDTO.class), eq(testUserId), eq(requesterId)))
                .thenReturn(updatedDTO);

        ResponseEntity<UserDTO> response = userController.updateUser(testUserId, userDTO, userDetails);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Updated Name", response.getBody().getName());
    }

    @Test
    void testUpdateUser_UserNotFound() {
        UserDTO userDTO = new UserDTO();
        userDTO.setName("Updated Name");

        when(userService.updateUser(any(UserDTO.class), eq(testUserId), eq(requesterId)))
                .thenReturn(null);

        ResponseEntity<UserDTO> response = userController.updateUser(testUserId, userDTO, userDetails);

        assertEquals(404, response.getStatusCodeValue());
        assertNull(response.getBody());
    }

    @Test
    void testDeactivateUser_Success() {
        when(userService.deactivateUser(testUserId)).thenReturn(true);

        ResponseEntity<String> response = userController.deactivateUser(testUserId, userDetails);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("deactivated successfully"));
    }

    @Test
    void testDeactivateUser_UserNotFound() {
        when(userService.deactivateUser(testUserId)).thenReturn(false);

        ResponseEntity<String> response = userController.deactivateUser(testUserId, userDetails);

        assertEquals(404, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("No user found") ||
                response.getBody().contains("not found") ||
                response.getBody().contains("User") && response.getBody().contains("found"));
    }
}