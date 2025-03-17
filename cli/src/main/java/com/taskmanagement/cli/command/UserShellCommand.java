package com.taskmanagement.cli.command;
import com.taskmanagement.cli.config.UserSession;
import com.taskmanagement.cli.service.APIService;
import com.taskmanagement.cli.service.ShellService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.shell.Availability;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;


import java.util.*;

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

    @ShellMethod(key = "user-update", value = "Update user details and role")
    @ShellMethodAvailability("isUserLoggedIn")
    public void updateUser(
            @ShellOption(help = "Name of the user to update") String name
    ) {
        try {
            // Step 1: Check if the user is authorized
            if (!isAuthorized()) {
                shellService.printError("You do not have permission to update users.");
                return;
            }
            shellService.printHeading("Fetching user by name: " + name);
            Object[] users = apiService.get("/users/search?name=" + name, Object[].class);

            if (users.length == 0) {
                shellService.printInfo("No users found with the name: " + name);
                return;
            }

            displayUsersTable(users);
            Map<String, Object> userToUpdate = (Map<String, Object>) users[0];
            String userId = String.valueOf(userToUpdate.get("id"));

            if (!isUserActive(userToUpdate)) {
                shellService.printError("You cannot update an inactive user.");
                return;
            }

            List<String> roleNames = fetchAvailableRoles();
            shellService.printHeading("Available roles:");
            roleNames.forEach(shellService::printInfo);

            String roleName = shellService.promptForInput("Enter the role name:").trim();

            Integer roleId = getRoleIdByName(roleName);
            if (roleId == null) {
                shellService.printError("Role not found with description: " + roleName);
                return;
            }

            Map<String, Object> userUpdateData = createUpdateData(userId, roleId);
            apiService.put("/users/" + userId, userUpdateData, Object.class);
            shellService.printSuccess("User updated successfully!");

            displayUsersTable(users);

        } catch (Exception e) {
            shellService.printError("Error updating user: " + e.getMessage());
        }
    }

    private boolean isAuthorized() {
        // Get the current authentication from the SecurityContext
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        var authorities = authentication.getAuthorities();
        return authorities.stream()
                .anyMatch(authority -> "ADMIN".equals(authority.getAuthority()));

    }

    private boolean isUserActive(Map<String, Object> user) {
        Boolean isActive = (Boolean) user.get("isActive");
        return isActive != null && isActive;
    }

    private List<String> fetchAvailableRoles() {
        Object[] roles = apiService.get("/roles", Object[].class);
        List<String> roleNames = new ArrayList<>();

        for (Object roleObj : roles) {
            Map<String, Object> role = (Map<String, Object>) roleObj;
            String roleName = (String) role.get("name");
            roleNames.add(roleName);
        }

        return roleNames;
    }

    private Integer getRoleIdByName(String roleName) {
        Object[] roles = apiService.get("/roles", Object[].class);
        for (Object roleObj : roles) {
            Map<String, Object> role = (Map<String, Object>) roleObj;
            if (roleName.equals(role.get("name"))) {
                return Integer.parseInt(String.valueOf(role.get("id")));
            }
        }
        return null;
    }

    private Map<String, Object> createUpdateData(String userId, Integer roleId) {
        return Map.of(
                "id", userId,
                "role", roleId,
                "isActive", true
        );
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


    @ShellMethod(key = "user-deactivate", value = "Deactivate a user")
    @ShellMethodAvailability("isUserLoggedIn")
    public void deactivateUser(@ShellOption(help = "Name of the user to deactivate") String name) {
        try {
            if (!isAuthorized()) {
                shellService.printError("You do not have permission to deactivate users.");
                return;
            }

            shellService.printHeading("Deactivating user by name: " + name);

            // Fetch users based on the provided name
            Object[] users = apiService.get("/users/search?name=" + name, Object[].class);

            if (users.length == 0) {
                shellService.printInfo("No users found with the name: " + name);
                return;
            }

            Map<String, Object> userToDeactivate = (Map<String, Object>) users[0];
            String userId = String.valueOf(userToDeactivate.get("id"));

            if (!isUserActive(userToDeactivate)) {
                shellService.printError("The user is already inactive.");
                return;
            }
            Map<String, Object> userUpdateData = new HashMap<>();
            userUpdateData.put("id", userId);
            userUpdateData.put("isActive", false);

            apiService.put("/users/" + userId, userUpdateData, Object.class);

            shellService.printSuccess("User " + name + " deactivated successfully!");

        } catch (Exception e) {
            shellService.printError("Error deactivating user: " + e.getMessage());
        }
    }



    private void displayUsersTable(Object[] users) {
        List<String[]> tableData = new ArrayList<>();

        for (Object userObj : users) {
            @SuppressWarnings("unchecked")
            Map<String, Object> user = (Map<String, Object>) userObj;


            String[] row = new String[5];
            row[0] = String.valueOf(user.get("id"));
            row[1] = String.valueOf(user.get("name"));
            row[2] = String.valueOf(user.get("email"));

            Map<String, Object> role = (Map<String, Object>) user.get("role");
            String roleName = role != null ? (String) role.get("name") : "No Role";
            row[3] = roleName;

            Boolean isActive = (Boolean) user.get("isActive");
            row[4] = (isActive != null && isActive) ? "Active" : "Inactive";

            tableData.add(row);
        }

        String[] headers = {"ID", "Name", "Email", "Role", "isActive"};

        shellService.printTable(headers, tableData.toArray(new String[0][]));
    }

    public Availability isUserLoggedIn() {
        return userSession.isAuthenticated()
                ? Availability.available()
                : Availability.unavailable("You are not logged in. Please use 'login' command first.");
    }
}
