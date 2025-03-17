package com.taskmanagement.cli.command;

import com.taskmanagement.cli.config.UserSession;
import com.taskmanagement.cli.service.APIService;
import com.taskmanagement.cli.service.ShellService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.Availability;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import java.time.ZonedDateTime;
import java.util.*;

import static com.taskmanagement.cli.util.DateUtils.parseDate;

@ShellComponent
public class SprintShellCommands {

    @Autowired
    private UserSession userSession;

    @Autowired
    private ShellService shellService;

    @Autowired
    private APIService apiService;


    @ShellMethod(key = "sprint-list", value = "List all sprints")
    @ShellMethodAvailability("isUserLoggedIn")
    public void listSprints() {
        if (!userSession.isAuthenticated()) {
            shellService.printError("You need to log in before listing sprints.");
            return;
        }

        try {
            List<Map<String, Object>> sprints = apiService.get("/sprints", List.class);

            if (sprints == null || sprints.isEmpty()) {
                shellService.printInfo("No sprints found.");
                return;
            }

            shellService.printSuccess("List of Sprints:");
            for (Map<String, Object> sprint : sprints) {
                shellService.printInfo("ID: " + sprint.get("id"));
                shellService.printInfo("Name: " + sprint.get("name"));
                shellService.printInfo("Goal: " + sprint.get("goal"));
                shellService.printInfo("Capacity Points: " + sprint.get("capacityPoints"));
                shellService.printInfo("Start Date: " + sprint.get("startDate"));
                shellService.printInfo("End Date: " + sprint.get("endDate"));
                shellService.printInfo("Is_active " + sprint.get("active"));
                shellService.printInfo("----------------------");
            }
        } catch (Exception e) {
            shellService.printError("Error fetching sprint list: " + e.getMessage());
        }
    }
    @ShellMethod(key = "sprint-get-id", value = "Get details of a specific sprint by ID")
    @ShellMethodAvailability("isUserLoggedIn")
    public void getSprintById() {
        if (!userSession.isAuthenticated()) {
            shellService.printError("You need to log in before retrieving sprint details.");
            return;
        }

        Scanner scanner = new Scanner(System.in);
        shellService.printInfo("Enter the Sprint ID to get details:");

        int sprintId;
        try {
            sprintId = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            shellService.printError("Invalid Sprint ID. Please enter a valid integer.");
            return;
        }

        try {
            // Get the specific sprint by its ID
            Map<String, Object> sprint = apiService.get("/sprints/" + sprintId, Map.class);

            if (sprint == null || sprint.isEmpty()) {
                shellService.printInfo("Sprint with ID " + sprintId + " not found.");
                return;
            }

            // Print sprint details
            shellService.printSuccess("Sprint details:");
            shellService.printInfo("ID: " + sprint.get("id"));
            shellService.printInfo("Name: " + sprint.get("name"));
            shellService.printInfo("Goal: " + sprint.get("goal"));
            shellService.printInfo("Capacity Points: " + sprint.get("capacityPoints"));
            shellService.printInfo("Start Date: " + sprint.get("startDate"));
            shellService.printInfo("End Date: " + sprint.get("endDate"));
            shellService.printInfo("Is_active: " + sprint.get("active"));
            shellService.printInfo("----------------------");
        } catch (Exception e) {
            shellService.printError("Error fetching sprint details: " + e.getMessage());
        }
    }

    @ShellMethod(key = "sprint-start", value = "Start a sprint")
    @ShellMethodAvailability("isUserLoggedIn")
    public void startSprint() {
        if (!userSession.isAuthenticated()) {
            shellService.printError("You need to log in before starting a sprint.");
            return;
        }

        Scanner scanner = new Scanner(System.in);
        shellService.printInfo("Enter the Sprint ID to start:");

        int sprintId;
        try {
            sprintId = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            shellService.printError("Invalid Sprint ID. Please enter a valid integer.");
            return;
        }

        try {
            Map<String, Object> sprintStatus = apiService.get("/sprints/" + sprintId, Map.class);
            sprintStatus.put("active", true);
            apiService.put("/sprints/" + sprintId, sprintStatus, Void.class);
            shellService.printSuccess("Sprint with ID " + sprintId + " has been started successfully!");

        } catch (Exception e) {
            shellService.printError("Error starting sprint: " + e.getMessage());
        }
    }


    @ShellMethod(key = "sprint-end", value = "End a sprint")
    @ShellMethodAvailability("isUserLoggedIn")
    public void endSprint() {
        if (!userSession.isAuthenticated()) {
            shellService.printError("You need to log in before starting a sprint.");
            return;
        }

        Scanner scanner = new Scanner(System.in);
        shellService.printInfo("Enter the Sprint ID to start:");

        int sprintId;
        try {
            sprintId = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            shellService.printError("Invalid Sprint ID. Please enter a valid integer.");
            return;
        }

        try {
            Map<String, Object> sprintStatus = apiService.get("/sprints/" + sprintId, Map.class);
            sprintStatus.put("active", false);
            apiService.put("/sprints/" + sprintId, sprintStatus, Void.class);
            shellService.printSuccess("Sprint with ID " + sprintId + " has been ended successfully!");

        } catch (Exception e) {
            shellService.printError("Error starting sprint: " + e.getMessage());
        }
    }

    @ShellMethod(key = "sprint-edit", value = "Edit a sprint's details")
    @ShellMethodAvailability("isUserLoggedIn")
    public void editSprint() {
        if (!userSession.isAuthenticated()) {
            shellService.printError("You need to log in before editing a sprint.");
            return;
        }

        Scanner scanner = new Scanner(System.in);
        shellService.printInfo("Enter the Sprint ID to edit:");

        int sprintId;
        try {
            sprintId = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            shellService.printError("Invalid Sprint ID. Please enter a valid integer.");
            return;
        }

        try {
            Map<String, Object> sprintStatus = apiService.get("/sprints/" + sprintId, Map.class);
            if (sprintStatus == null || sprintStatus.isEmpty()) {
                shellService.printError("Sprint not found.");
                return;
            }

            shellService.printSuccess("Sprint details:");
            shellService.printInfo("ID: " + sprintStatus.get("id"));
            shellService.printInfo("Name: " + sprintStatus.get("name"));
            shellService.printInfo("Goal: " + sprintStatus.get("goal"));
            shellService.printInfo("Capacity Points: " + sprintStatus.get("capacityPoints"));
            shellService.printInfo("Start Date: " + sprintStatus.get("startDate"));
            shellService.printInfo("End Date: " + sprintStatus.get("endDate"));
            shellService.printInfo("Is Active: " + sprintStatus.get("active"));
            shellService.printInfo("----------------------");

            shellService.printInfo("Which fields would you like to edit? (comma-separated list of fields like: name, goal, capacityPoints, startDate, endDate):");
            String fieldsToEditInput = scanner.nextLine().trim().toLowerCase();

            String[] fieldsToEdit = fieldsToEditInput.split("\\s*,\\s*");
            Set<String> validFields = new HashSet<>(Arrays.asList("name", "goal", "capacitypoints", "startdate", "enddate"));

            for (String field : fieldsToEdit) {
                if (!validFields.contains(field)) {
                    shellService.printError("Invalid field: " + field + ". Please choose from: name, goal, capacityPoints, startDate, endDate.");
                    return;
                }
            }
            for (String field : fieldsToEdit) {
                shellService.printInfo("Enter the new value for " + field + ":");
                String newValue = scanner.nextLine().trim();
                sprintStatus.put(field, newValue);
            }
            apiService.put("/sprints/" + sprintId, sprintStatus, Void.class);
            shellService.printSuccess("Sprint with ID " + sprintId + " has been updated successfully!");

        } catch (Exception e) {
            shellService.printError("Error editing sprint: " + e.getMessage());
        }
    }

    @ShellMethod(key = "sprint-owner", value = "Find the owner/scrum master of a sprint")
    @ShellMethodAvailability("isUserLoggedIn")
    public void findSprintOwner() {
        if (!userSession.isAuthenticated()) {
            shellService.printError("You need to log in before fetching the sprint owner.");
            return;
        }
        Scanner scanner = new Scanner(System.in);
        shellService.printInfo("Enter the Sprint ID to find the owner or scrum master:");

        int sprintId;
        try {
            sprintId = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            shellService.printError("Invalid Sprint ID. Please enter a valid integer.");
            return;
        }

        try {
            Map<String, Object> sprintStatus = apiService.get("/sprints/" + sprintId, Map.class);

            if (sprintStatus == null || sprintStatus.isEmpty()) {
                shellService.printError("Sprint with ID " + sprintId + " not found.");
                return;
            }
            Object ownerId = sprintStatus.get("scrumMasterId");
            if (ownerId == null) {
                shellService.printError("No owner or scrum master found for Sprint ID " + sprintId);
                return;
            }
            Map<String, Object> user = apiService.get("/users/" + ownerId, Map.class);
            if (user == null) {
                shellService.printError("Error retrieving user details for owner/scrum master.");
                return;
            }
            String userName = (String) user.get("name");
            shellService.printSuccess("Owner/Scrum Master of Sprint ID " + sprintId + ": " + userName);

        } catch (Exception e) {
            shellService.printError("Error finding sprint owner: " + e.getMessage());
        }
    }

    @ShellMethod(key = "sprint-create", value = "Create a new sprint")
    @ShellMethodAvailability("isUserLoggedIn")
    public void createSprint() {
        if (!userSession.isAuthenticated()) {
            shellService.printError("You need to log in before creating a sprint.");
            return;
        }
        Scanner scanner = new Scanner(System.in);

        String loggedInUserRole = userSession.getUserRole();
        String loggedInUserId = userSession.getUserId();

        UUID scrumMasterId;

        if ("SCRUM_MASTER".equalsIgnoreCase(loggedInUserRole)) {
            scrumMasterId = UUID.fromString(loggedInUserId);
            shellService.printInfo("You are a Scrum Master, and your ID will be used for the Scrum Master.");
        } else {
            shellService.printInfo("Enter Scrum Master's ID (UUID format):");
            String scrumMasterIdInput = scanner.nextLine().trim();

            try {
                scrumMasterId = UUID.fromString(scrumMasterIdInput);
            } catch (IllegalArgumentException e) {
                shellService.printError("Invalid UUID format. Please enter a valid UUID.");
                return;
            }
            shellService.printInfo("Scrum Master ID accepted: " + scrumMasterId);
        }
        shellService.printInfo("Enter sprint name:");
        String name = scanner.nextLine().trim();

        shellService.printInfo("Enter sprint goal:");
        String goal = scanner.nextLine().trim();

        shellService.printInfo("Enter sprint capacity points:");
        int capacityPoints = Integer.parseInt(scanner.nextLine().trim());

        shellService.printInfo("Enter sprint start date (yyyy-MM-dd):");
        String startDateInput = scanner.nextLine().trim();
        ZonedDateTime startDate = parseDate(startDateInput);
        if (startDate == null) {
            shellService.printError("Invalid start date format. Please use yyyy-MM-dd.");
            return;
        }

        shellService.printInfo("Enter sprint end date (yyyy-MM-dd):");
        String endDateInput = scanner.nextLine().trim();
        ZonedDateTime endDate = parseDate(endDateInput);
        if (endDate == null) {
            shellService.printError("Invalid end date format. Please use yyyy-MM-dd.");
            return;
        }

        Map<String, Object> sprintData = new HashMap<>();
        sprintData.put("name", name);
        sprintData.put("goal", goal);
        sprintData.put("capacityPoints", capacityPoints);
        sprintData.put("startDate", startDate);
        sprintData.put("endDate", endDate);
        sprintData.put("scrumMasterId", scrumMasterId);

        try {
            Map<String, Object> response = apiService.post("/sprints", sprintData, Map.class);

            if (response != null) {
                shellService.printSuccess("Sprint created successfully!");
                shellService.printInfo("Sprint ID: " + response.get("id"));
            } else {
                shellService.printError("Failed to create sprint. Response is null.");
            }
        } catch (Exception e) {
            shellService.printError("Error creating sprint: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Availability isUserLoggedIn() {
        return userSession.isAuthenticated()
                ? Availability.available()
                : Availability.unavailable("you are not logged in. Please use 'login' command first");
    }
}