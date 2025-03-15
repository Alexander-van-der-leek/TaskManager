package com.taskmanagement.cli.command;

import com.taskmanagement.cli.config.UserSession;
import com.taskmanagement.cli.service.ShellService;
import com.taskmanagement.dto.UserDTO;
import com.taskmanagement.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.Availability;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;

import java.util.List;
import java.util.UUID;

@ShellComponent
public class UserShellCommand {

    @Autowired
    private UserSession userSession;

    @Autowired
    private UserService userService;

    @Autowired
    private ShellService shellService;

    @ShellMethod(key = "user-list", value = "List all users")
    @ShellMethodAvailability("isUserLoggedIn")
    public void listUsers() {
        List<UserDTO> users = userService.getAllUsers(UUID.fromString(userSession.getUserId()));
        users.forEach(user -> {
            shellService.printInfo("User ID: " + user.getId() + ", Name: " + user.getName() + ", Email: " + user.getEmail());
        });
    }

    @ShellMethod(key = "user-create", value = "Create a new user")
    @ShellMethodAvailability("isUserLoggedIn")
    public void createUser(String name, String email, String googleId) {

        UserDTO userDTO = UserDTO.builder()
                .name(name)
                .email(email)
                .googleId(googleId)
                .build();
        UserDTO createdUser = userService.createUser(userDTO, UUID.fromString(userSession.getUserId()));
        shellService.printInfo("User created successfully: " + createdUser.getName());
    }

    @ShellMethod(key = "user-update", value = "Update user details")
    @ShellMethodAvailability("isUserLoggedIn")
    public void updateUser(UUID userId, String name, String email, String googleId) {
        UserDTO userDTO = UserDTO.builder()
                .id(userId)
                .name(name)
                .email(email)
                .googleId(googleId)
                .build();
        UserDTO updatedUser = userService.updateUser(userDTO, UUID.fromString(userSession.getUserId()));
        shellService.printInfo("User updated successfully: " + updatedUser.getName());
    }


    @ShellMethod(key = "user-deactivate", value = "Deactivate (soft delete) a user")
    @ShellMethodAvailability("isUserLoggedIn")
    public void deactivateUser(UUID userId) {
        userService.deactivateUser(userId, UUID.fromString(userSession.getUserId()));
        shellService.printInfo("User with ID " + userId + " has been deactivated.");
    }

    @ShellMethod(key = "user-info", value = "Get user details by ID")
    @ShellMethodAvailability("isUserLoggedIn")
    public void userInfo(UUID userId) {
        UserDTO user = userService.getUserById(userId, UUID.fromString(userSession.getUserId()));
        shellService.printInfo("User Info: " + user.getName() + " | " + user.getEmail() + " | " + user.getRole().getName());
    }

}
