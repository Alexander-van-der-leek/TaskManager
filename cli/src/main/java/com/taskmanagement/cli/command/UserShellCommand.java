package com.taskmanagement.cli.command;

import com.taskmanagement.cli.config.UserSession;
import com.taskmanagement.cli.service.APIService;
import com.taskmanagement.cli.service.ShellService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.Availability;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ShellComponent
public class UserShellCommand {

    @Autowired
    private APIService apiService;

    @Autowired
    private UserSession userSession;

    @Autowired
    private ShellService shellService;

    @ShellMethod(key = "user-list", value = "List all users")
    @ShellMethodAvailability("isUserLoggedIn")
    public void listUsers() {
        try {
            shellService.printHeading("Fetching Users...");

            Object[] users = apiService.get("/users", Object[].class);
            if (users.length == 0) {
                shellService.printInfo("No users found");
            } else {
                displayUsersTable(users);
            }
        } catch (Exception e) {
            shellService.printError("Error fetching users: " + e.getMessage());
        }
    }

    @ShellMethod(key = "user-search", value = "Search for users by name")
    @ShellMethodAvailability("isUserLoggedIn")
    public void searchUsers(@ShellOption(help = "Name to search for") String name) {
        try {
            shellService.printHeading("Searching for users with name containing: " + name);

            Object[] users = apiService.get("/users/search?name=" + name, Object[].class);
            if (users.length == 0) {
                shellService.printInfo("No users found matching the search term");
            } else {
                displayUsersTable(users);
            }
        } catch (Exception e) {
            shellService.printError("Error searching users: " + e.getMessage());
        }
    }

    @ShellMethod(key = "user-get", value = "Get user details by ID")
    @ShellMethodAvailability("isUserLoggedIn")
    public void getUser(@ShellOption(help = "User ID") String userId) {
        try {
            shellService.printHeading("Fetching user details...");

            Object userObj = apiService.get("/users/" + userId, Object.class);
            @SuppressWarnings("unchecked")
            Map<String, Object> user = (Map<String, Object>) userObj;

            shellService.printHeading("User Details:");
            shellService.printInfo("ID: " + user.get("id"));
            shellService.printInfo("Name: " + user.get("name"));
            shellService.printInfo("Email: " + user.get("email"));
            shellService.printInfo("Role: " + user.get("roleName"));
        } catch (Exception e) {
            shellService.printError("Error fetching user: " + e.getMessage());
        }
    }

    // Helper method to display users table
    private void displayUsersTable(Object[] users) {
        List<String[]> tableData = new ArrayList<>();

        for (Object userObj : users) {
            @SuppressWarnings("unchecked")
            Map<String, Object> user = (Map<String, Object>) userObj;

            String[] row = new String[3];
            row[0] = String.valueOf(user.get("id"));
            row[1] = String.valueOf(user.get("name"));
            row[2] = String.valueOf(user.get("email"));

            tableData.add(row);
        }

        String[] headers = {"ID", "Name", "Email"};
        shellService.printTable(headers, tableData.toArray(new String[0][]));
    }

    public Availability isUserLoggedIn() {
        return userSession.isAuthenticated()
                ? Availability.available()
                : Availability.unavailable("you are not logged in. Please use 'login' command first");
    }
}