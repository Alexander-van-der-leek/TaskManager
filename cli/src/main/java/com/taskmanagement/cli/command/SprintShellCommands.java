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

            List<String[]> tableData = new ArrayList<>();

            for (Map<String, Object> sprint : sprints) {
                String[] row = new String[7];
                row[0] = String.valueOf(sprint.get("id"));
                row[1] = String.valueOf(sprint.get("name"));
                row[2] = String.valueOf(sprint.get("goal"));
                row[3] = String.valueOf(sprint.get("capacityPoints"));
                row[4] = String.valueOf(sprint.get("startDate"));
                row[5] = String.valueOf(sprint.get("endDate"));
                row[6] = String.valueOf(sprint.get("active"));

                tableData.add(row);
            }
            String[] headers = {"ID", "Name", "Goal", "Capacity", "Start Date", "End Date", "Is Active"};
            shellService.printTable(headers, tableData.toArray(new String[0][]));

        } catch (Exception e) {
            shellService.printError("Error could not fetch the sprint list: ");
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
            Map<String, Object> sprint = apiService.get("/sprints/" + sprintId, Map.class);

            if (sprint == null || sprint.isEmpty()) {
                shellService.printInfo("Sprint with ID " + sprintId + " not found.");
                return;
            }
            String[] headers = {"ID", "Name", "Goal", "Capacity", "Start Date", "End Date", "Is Active"};
            String[] row = {
                    String.valueOf(sprint.get("id")),
                    String.valueOf(sprint.get("name")),
                    String.valueOf(sprint.get("goal")),
                    String.valueOf(sprint.get("capacityPoints")),
                    String.valueOf(sprint.get("startDate")),
                    String.valueOf(sprint.get("endDate")),
                    String.valueOf(sprint.get("active"))
            };
            shellService.printTable(headers, new String[][]{row});

        } catch (Exception e) {
            shellService.printError("Error could not fetch the sprint details: ");
        }
    }

    @ShellMethod(key = "sprint-start", value = "Start a sprint")
    @ShellMethodAvailability("isUserLoggedIn")
    public void startSprint() {
        if (!userSession.isAuthenticated()) {
            shellService.printError("You need to log in before starting a sprint.");
            return;
        }
        try {
            List<Map<String, Object>> sprints = apiService.get("/sprints", List.class);
            if (sprints == null || sprints.isEmpty()) {
                shellService.printInfo("No sprints available.");
                return;
            }
            shellService.printInfo("List of all sprints:");
            for (Map<String, Object> sprint : sprints) {
                String[] headers = {"ID", "Name", "Goal", "Capacity", "Start Date", "End Date", "Is Active"};
                String[] row = {
                        String.valueOf(sprint.get("id")),
                        String.valueOf(sprint.get("name")),
                        String.valueOf(sprint.get("goal")),
                        String.valueOf(sprint.get("capacityPoints")),
                        String.valueOf(sprint.get("startDate")),
                        String.valueOf(sprint.get("endDate")),
                        String.valueOf(sprint.get("active"))
                };
                shellService.printTable(headers, new String[][]{row});
            }
        } catch (Exception e) {
            shellService.printError("Error could not fetch the sprint Id: ");
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
            shellService.printError("Error could not start the sprint");
        }
    }

    @ShellMethod(key = "sprint-end", value = "End a sprint")
    @ShellMethodAvailability("isUserLoggedIn")
    public void endSprint() {
        if (!userSession.isAuthenticated()) {
            shellService.printError("You need to log in before ending a sprint.");
            return;
        }
        try {
            List<Map<String, Object>> sprints = apiService.get("/sprints", List.class);
            if (sprints == null || sprints.isEmpty()) {
                shellService.printInfo("No sprints available.");
                return;
            }
            shellService.printInfo("List of all sprints:");
            for (Map<String, Object> sprint : sprints) {
                String[] headers = {"ID", "Name", "Goal", "Capacity", "Start Date", "End Date", "Is Active"};
                String[] row = {
                        String.valueOf(sprint.get("id")),
                        String.valueOf(sprint.get("name")),
                        String.valueOf(sprint.get("goal")),
                        String.valueOf(sprint.get("capacityPoints")),
                        String.valueOf(sprint.get("startDate")),
                        String.valueOf(sprint.get("endDate")),
                        String.valueOf(sprint.get("active"))
                };
                shellService.printTable(headers, new String[][]{row});
            }
        } catch (Exception e) {
            shellService.printError("Error could not fetch the sprint Id: ");
            return;
        }

        Scanner scanner = new Scanner(System.in);
        shellService.printInfo("Enter the Sprint ID to end:");

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
            shellService.printError("Error could not end the sprint");
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
            Map<String, Object> sprintEdit = apiService.get("/sprints/" + sprintId, Map.class);
            if (sprintEdit == null || sprintEdit.isEmpty()) {
                shellService.printError("Sprint not found.");
                return;
            }

            String[] headers = {"ID", "Name", "Goal", "Capacity", "Start Date", "End Date", "Is Active"};
            String[] row = {
                    String.valueOf(sprintEdit.get("id")),
                    String.valueOf(sprintEdit.get("name")),
                    String.valueOf(sprintEdit.get("goal")),
                    String.valueOf(sprintEdit.get("capacityPoints")),
                    String.valueOf(sprintEdit.get("startDate")),
                    String.valueOf(sprintEdit.get("endDate")),
                    String.valueOf(sprintEdit.get("active"))
            };

            shellService.printTable(headers, new String[][]{row});

            shellService.printInfo("Which fields would you like to edit? (comma-separated list: name, goal, capacityPoints, startDate, endDate):");
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
                sprintEdit.put(field, newValue);
            }

            apiService.put("/sprints/" + sprintId, sprintEdit, Void.class);
            shellService.printSuccess("Sprint with ID " + sprintId + " has been updated successfully!");

        } catch (Exception e) {
            shellService.printError("Error could not edit the sprint");
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

            Map<String, Object> user = apiService.get("/users/search" + ownerId, Map.class);

            if (user == null) {
                shellService.printError("Error retrieving user details for owner/scrum master.");
                return;
            }

            String userName = (String) user.get("name");
            shellService.printSuccess("Owner/Scrum Master of Sprint ID " + sprintId + ": " + userName);

        } catch (Exception e) {
            shellService.printError("Error could not find the sprint owner");
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

        UUID scrumMasterId = null;

        if ("SCRUM_MASTER".equalsIgnoreCase(loggedInUserRole)){
            scrumMasterId = UUID.fromString(loggedInUserId);
            shellService.printInfo("You are a Scrum Master, and your ID will be used for the Scrum Master.");
        } else {
            shellService.printInfo("Enter the name of the Scrum Master:");
            String scrumMasterName = scanner.nextLine().trim();

            try {
                Object[] users = apiService.get("/users/search?name=" + scrumMasterName, Object[].class);

                if (users != null && users.length > 0) {
                    Map<String, Object> scrumMaster = (Map<String, Object>) users[0];

                    if (scrumMaster.containsKey("id")) {
                        scrumMasterId = UUID.fromString((String) scrumMaster.get("id"));
                        shellService.printInfo("Scrum Master found. ID: " + scrumMasterId);
                    } else {
                        shellService.printError("Scrum Master ID not found in response.");
                        return;
                    }
                } else {
                    shellService.printError("Scrum Master with name " + scrumMasterName + " not found.");
                    return;
                }
            } catch (Exception e) {
                shellService.printError("Error could not fetch the Scrum Master's ID");
                return;
            }
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
            shellService.printError("Error could not create the sprint");
        }
    }

    public Availability isUserLoggedIn() {
        return userSession.isAuthenticated()
                ? Availability.available()
                : Availability.unavailable("you are not logged in. Please use 'login' command first");
    }
}