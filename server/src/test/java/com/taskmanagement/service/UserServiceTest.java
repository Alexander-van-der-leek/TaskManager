package com.taskmanagement.service;

import com.taskmanagement.dto.UserDTO;
import com.taskmanagement.exception.ResourceNotFound;
import com.taskmanagement.model.User;
import com.taskmanagement.repository.UserRepository;
import com.taskmanagement.exception.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    private UUID testUserId;
    private UUID requesterId;
    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testUserId = UUID.randomUUID();
        requesterId = UUID.randomUUID();

        testUser = new User();
        testUser.setId(testUserId);
        testUser.setName("noluthandoh14");
        testUser.setEmail("noluthandoh14@gmail.com");
        testUser.setIsActive(true);
    }

    @Test
    void testGetAllUsers() {
        when(userRepository.findAll()).thenReturn(Collections.singletonList(testUser));

        List<UserDTO> users = userService.getAllUsers();

        assertNotNull(users);
        assertEquals(1, users.size());
        assertEquals("noluthandoh14", users.get(0).getName());
        assertEquals("noluthandoh14@gmail.com", users.get(0).getEmail());
    }

    @Test
    void testSearchUsersByName() {
        String name = "noluthandoh14";
        when(userRepository.findByNameContainingIgnoreCase(name)).thenReturn(Collections.singletonList(testUser));

        List<UserDTO> users = userService.searchUsersByName(name);

        assertNotNull(users);
        assertEquals(1, users.size());
        assertEquals("noluthandoh14", users.get(0).getName());
        assertEquals("noluthandoh14@gmail.com", users.get(0).getEmail());
    }

    @Test
    void testUpdateUser_Success() {
        UserDTO userDTO = new UserDTO();
        userDTO.setName("Updated Name");

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setName("Updated Name");
            return savedUser;
        });

        UserDTO updatedUser = userService.updateUser(userDTO, testUserId, requesterId);

        assertNotNull(updatedUser);
        assertEquals("Updated Name", updatedUser.getName());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testUpdateUser_UserNotFound() {
        UserDTO userDTO = new UserDTO();
        userDTO.setName("Updated Name");

        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFound.class, () -> {
            userService.updateUser(userDTO, testUserId, requesterId);
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testDeactivateUser_Success() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        boolean result = userService.deactivateUser(testUserId);

        assertTrue(result);
        assertFalse(testUser.getIsActive());
        verify(userRepository).save(testUser);
    }

    @Test
    void testDeactivateUser_UserNotFound() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> {
            userService.deactivateUser(testUserId);
        });

        verify(userRepository, never()).save(any(User.class));
    }
}