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

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllUsers() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setName("noluthandoh14");
        user.setEmail("noluthandoh14@gmail.com");

        when(userRepository.findAll()).thenReturn(Collections.singletonList(user));

        List<UserDTO> users = userService.getAllUsers();

        assertNotNull(users);
        assertEquals(1, users.size());
        assertEquals("noluthandoh14", users.get(0).getName());
        assertEquals("noluthandoh14@gmail.com", users.get(0).getEmail());
    }

    @Test
    void testSearchUsersByName() {
        String name = "noluthandoh14";
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setName(name);
        user.setEmail("noluthandoh14@gmail.com");

        when(userRepository.findByNameContainingIgnoreCase(name)).thenReturn(Collections.singletonList(user));

        List<UserDTO> users = userService.searchUsersByName(name);

        assertNotNull(users);
        assertEquals(1, users.size());
        assertEquals("noluthandoh14", users.get(0).getName());
        assertEquals("noluthandoh14@gmail.com", users.get(0).getEmail());
    }

    @Test
    void testUpdateUser() {
        UUID userId = UUID.randomUUID();
        UserDTO userDTO = new UserDTO();
        userDTO.setName("Updated Name");

        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setName("noluthandoh14");
        existingUser.setEmail("noluthandoh14@gmail.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        UserDTO updatedUser = userService.updateUser(userDTO, "noluthandoh14", userId);

        assertNotNull(updatedUser);
        assertEquals("Updated Name", updatedUser.getName());
    }

    @Test
    void testDeactivateUser_Success() {
        String name = "noluthandoh14";
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setName(name);
        user.setEmail("noluthandoh14@gmail.com");
        user.setIsActive(true);

        when(userRepository.findByNameContainingIgnoreCase(name)).thenReturn(Collections.singletonList(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        boolean result = userService.deactivateUser(name);

        assertTrue(result);
        assertFalse(user.getIsActive());
    }

    @Test
    void testDeactivateUser_UserNotFound() {
        String name = "noluthandoh14";
        when(userRepository.findByNameContainingIgnoreCase(name)).thenReturn(Collections.emptyList());

        assertThrows(UserNotFoundException.class, () -> userService.deactivateUser(name));
    }
}
