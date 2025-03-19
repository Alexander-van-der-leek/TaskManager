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
            shellService.printHeading("Searching for user by name: " + name);

            Object[] users = apiService.get("/users/search?name=" + name, Object[].class);

            if (users.length == 0) {
                shellService.printInfo("No users found with the name: " + name);
                return;
            }

            displayUsersTable(users);

            String userId;
            if (users.length > 1) {
                String userIdInput = shellService.promptForInput("Multiple users found. Enter the ID of the user to update:").trim();
                userId = userIdInput;
            } else {
                @SuppressWarnings("unchecked")
                Map<String, Object> user = (Map<String, Object>) users[0];
                userId = String.valueOf(user.get("id"));
                shellService.printInfo("Selected user with ID: " + userId);
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> selectedUser = null;
            for (Object userObj : users) {
                @SuppressWarnings("unchecked")
                Map<String, Object> user = (Map<String, Object>) userObj;
                if (userId.equals(String.valueOf(user.get("id")))) {
                    selectedUser = user;
                    break;
                }
            }
            shellService.printHeading("Searching for user by name: " + name);

            if (selectedUser == null) {
                shellService.printError("Selected user not found in search results.");
                return;
            }

            if (!isUserActive(selectedUser)) {
                shellService.printError("You cannot update an inactive user.");
                return;
            }
            shellService.printHeading("Searching for user by name: " + name);

            List<String> roleNames = fetchAvailableRoles();
            shellService.printHeading("Available roles:");
            roleNames.forEach(shellService::printInfo);

            shellService.printHeading("Searching for user by name: " + name);

            Scanner scanner = new Scanner(System.in);

            shellService.promptForInput("Enter the role name:");
            String roleName = scanner.nextLine().trim();

            Integer roleId = getRoleIdByName(roleName);
            if (roleId == null) {
                shellService.printError("Role not found with description: " + roleName);
                return;
            }

            shellService.printHeading("Searching for user by name: " + name);

            Map<String, Object> userUpdateData = createUpdateData(userId, roleId);
            apiService.put("/users/" + userId, userUpdateData, Object.class);
            shellService.printSuccess("User updated successfully!");


        } catch (Exception e) {
            shellService.printError("Error updating user: " + e.getMessage());
        }
    }

    private boolean isAuthorized() {
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
        try {
            Object[] roles = apiService.get("/roles", Object[].class);
            List<String> roleNames = new ArrayList<>();

            for (Object roleObj : roles) {
                @SuppressWarnings("unchecked")
                Map<String, Object> role = (Map<String, Object>) roleObj;
                String roleName = (String) role.get("name");
                roleNames.add(roleName);
            }

            return roleNames;
        } catch (Exception e) {
            shellService.printError("Error fetching roles: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    private Integer getRoleIdByName(String roleName) {
        try {
            if (roleName == null || roleName.trim().isEmpty()) {
                return null;
            }

            roleName = roleName.trim().toLowerCase();
            Object[] roles = apiService.get("/roles", Object[].class);

            for (Object roleObj : roles) {
                @SuppressWarnings("unchecked")
                Map<String, Object> role = (Map<String, Object>) roleObj;
                String name = ((String) role.get("name")).toLowerCase();

                if (name.equals(roleName)) {
                    return Integer.parseInt(String.valueOf(role.get("id")));
                }
            }

            List<Map<String, Object>> partialMatches = new ArrayList<>();

            for (Object roleObj : roles) {
                @SuppressWarnings("unchecked")
                Map<String, Object> role = (Map<String, Object>) roleObj;
                String name = ((String) role.get("name")).toLowerCase();

                if (name.contains(roleName) || roleName.contains(name)) {
                    partialMatches.add(role);
                }
            }

            if (partialMatches.size() == 1) {
                return Integer.parseInt(String.valueOf(partialMatches.get(0).get("id")));
            } else if (partialMatches.size() > 1) {
                shellService.printWarning("Multiple roles match '" + roleName + "'. Please select one:");
                for (int i = 0; i < partialMatches.size(); i++) {
                    shellService.printInfo((i + 1) + ". " + partialMatches.get(i).get("name"));
                }

                String selection = shellService.promptForInput("Enter number of role to use:").trim();
                try {
                    int index = Integer.parseInt(selection) - 1;
                    if (index >= 0 && index < partialMatches.size()) {
                        return Integer.parseInt(String.valueOf(partialMatches.get(index).get("id")));
                    } else {
                        shellService.printError("Invalid selection.");
                        return null;
                    }
                } catch (NumberFormatException e) {
                    shellService.printError("Invalid input. Please enter a number.");
                    return null;
                }
            }

            return null;

        } catch (Exception e) {
            shellService.printError("Error getting role ID: " + e.getMessage());
            return null;
        }
    }

    private Map<String, Object> createUpdateData(String userId, Integer roleId) {
        Map<String, Object> role = new HashMap<>();
        role.put("id", roleId);
        Map<String, Object> userData = new HashMap<>();
        userData.put("id", userId);
        userData.put("role", role);
        userData.put("isActive", true);

        return userData;
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
            shellService.printHeading("Searching for user by name: " + name);

            Object[] users = apiService.get("/users/search?name=" + name, Object[].class);

            if (users.length == 0) {
                shellService.printInfo("No users found with the name: " + name);
                return;
            }

            displayUsersTable(users);

            String userId;
            if (users.length > 1) {
                String userIdInput = shellService.promptForInput("Multiple users found. Enter the ID of the user to deactivate:").trim();
                userId = userIdInput;
            } else {
                @SuppressWarnings("unchecked")
                Map<String, Object> user = (Map<String, Object>) users[0];
                userId = String.valueOf(user.get("id"));
                shellService.printInfo("Selected user with ID: " + userId);
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> selectedUser = null;
            for (Object userObj : users) {
                @SuppressWarnings("unchecked")
                Map<String, Object> user = (Map<String, Object>) userObj;
                if (userId.equals(String.valueOf(user.get("id")))) {
                    selectedUser = user;
                    break;
                }
            }

            if (selectedUser == null) {
                shellService.printError("Selected user not found in search results.");
                return;
            }

            if (!isUserActive(selectedUser)) {
                shellService.printError("User is already inactive.");
                return;
            }

            Map<String, Object> userUpdateData = new HashMap<>();
            userUpdateData.put("id", userId);
            userUpdateData.put("isActive", false);

            apiService.put("/users/" + userId, userUpdateData, Object.class);
            shellService.printSuccess("User " + selectedUser.get("name") + " deactivated successfully!");

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
