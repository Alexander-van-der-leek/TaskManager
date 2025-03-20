package com.taskmanagement.cli.command;

import com.taskmanagement.cli.config.UserSession;
import com.taskmanagement.cli.service.APIService;
import com.taskmanagement.cli.service.ShellService;
import com.taskmanagement.cli.util.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.Availability;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import static com.taskmanagement.cli.util.DateUtils.parseDate;

@ShellComponent
public class EpicShellCommand {

    @Autowired
    private APIService apiService;

    @Autowired
    private UserSession userSession;

    @Autowired
    private ShellService shellService;

    @ShellMethod(key = "epic-search", value = "Search for epics by name")
    @ShellMethodAvailability("isUserLoggedIn")
    public void searchEpics(@ShellOption(help = "Name to search for") String name) {
        try {
            shellService.printHeading("Searching for epics with name containing: " + name);

            Object[] epics = apiService.get("/epics/search?name=" + name, Object[].class);
            if (epics.length == 0) {
                shellService.printInfo("No epics found matching the search term");
            } else {
                List<String[]> tableData = new ArrayList<>();
                for (Object epicObj : epics) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> epic = (Map<String, Object>) epicObj;

                    String[] row = new String[3];
                    row[0] = String.valueOf(epic.get("id"));
                    row[1] = String.valueOf(epic.get("name"));
                    row[2] = String.valueOf(epic.get("ownerName"));

                    tableData.add(row);
                }

                String[] headers = {"ID", "Name", "Owner Name"};
                shellService.printTable(headers, tableData.toArray(new String[0][]));
            }
        } catch (Exception e) {
            shellService.printError("Error searching epics: " + e.getMessage());
        }
    }

    @ShellMethod(key = "epic-list", value = "List all epics")
    @ShellMethodAvailability("isUserLoggedIn")
    public void listEpics() {
        try {
            shellService.printHeading("Fetching Epics...");

            Object[] epics = apiService.get("/epics", Object[].class);
            if (epics.length == 0) {
                shellService.printInfo("No epics found");
            } else {
                List<String[]> tableData = new ArrayList<>();
                for (Object epicObj : epics) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> epic = (Map<String, Object>) epicObj;

                    String[] row = new String[3];
                    row[0] = String.valueOf(epic.get("id"));
                    row[1] = String.valueOf(epic.get("name"));
                    row[2] = String.valueOf(epic.get("ownerName"));

                    tableData.add(row);
                }

                String[] headers = {"ID", "Name", "Owner Name"};
                shellService.printTable(headers, tableData.toArray(new String[0][]));
            }
        } catch (Exception e) {
            shellService.printError("Error fetching epics: " + e.getMessage());
        }
    }

    @ShellMethod(key = "epic-create", value = "Create a new epic")
    @ShellMethodAvailability("isUserLoggedIn")
    public void createEpic(
            @ShellOption(value = {"-n", "--name"}, help = "Epic name") String name,
            @ShellOption(value = {"-d", "--desc"}, help = "Epic description") String description,
            @ShellOption(value = {"-o", "--owner-name"}, help = "Owner name") String ownerName,
            @ShellOption(value = {"-sp", "--story-points"}, help = "Story points", defaultValue = "0") Integer storyPoints,
            @ShellOption(value = {"-s", "--start-date"}, help = "Start date (YYYY-MM-DD)", defaultValue = ShellOption.NULL) String startDate,
            @ShellOption(value = {"-te", "--target-end-date"}, help = "Target end date (YYYY-MM-DD)", defaultValue = ShellOption.NULL) String targetEndDate
    ) {
        try {
            shellService.printHeading("Creating new epic...");

            Object[] users = apiService.get("/users/search?name=" + ownerName, Object[].class);

            if (users.length == 0) {
                shellService.printError("No user found with name containing: " + ownerName);
                return;
            }

            if (users.length > 1) {
                shellService.printWarning("Multiple users found with that name. Please be more specific:");
                //displayUsersTable(users);
                return;
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> user = (Map<String, Object>) users[0];
            String ownerId = String.valueOf(user.get("id"));

            Map<String, Object> epic = new HashMap<>();
            epic.put("name", name);
            epic.put("description", description);
            epic.put("ownerId", ownerId);
            epic.put("storyPoints", storyPoints);
            epic.put("startDate", parseDate(startDate));
            epic.put("targetEndDate", parseDate(targetEndDate));

            Object createdEpic = apiService.post("/epics", epic, Object.class);
            shellService.printSuccess("Epic created successfully!");

            @SuppressWarnings("unchecked")
            Map<String, Object> epicResult = (Map<String, Object>) createdEpic;
            shellService.printInfo("ID: " + epicResult.get("id"));
            shellService.printInfo("Name: " + epicResult.get("name"));
        } catch (Exception e) {
            shellService.printError("Error creating epic: " + e.getMessage());
        }
    }

    @ShellMethod(key = "epic-get", value = "Get epic details")
    @ShellMethodAvailability("isUserLoggedIn")
    public void getEpic(@ShellOption(help = "Epic ID") String epicId) {
        try {
            shellService.printHeading("Fetching epic details...");
            Object epicObj = apiService.get("/epics/" + epicId, Object.class);

            @SuppressWarnings("unchecked")
            Map<String, Object> epic = (Map<String, Object>) epicObj;
            shellService.printHeading("Epic Details:");
            shellService.printInfo("ID: " + epic.get("id"));
            shellService.printInfo("Name: " + epic.get("name"));
            shellService.printInfo("Description: " + epic.get("description"));
            shellService.printInfo("Owner: " + epic.get("ownerName"));
            shellService.printInfo("Story Points: " + epic.get("storyPoints"));
            shellService.printInfo("Start Date: " + epic.get("startDate"));
            shellService.printInfo("Target End Date: " + epic.get("targetEndDate"));
        } catch (Exception e) {
            shellService.printError("Error fetching epic: " + e.getMessage());
        }
    }

    @ShellMethod(key = "epic-update", value = "Update an existing epic")
    @ShellMethodAvailability("isUserLoggedIn")
    public void updateEpic(
            @ShellOption(help = "Epic ID") String epicId,
            @ShellOption(value = {"-n", "--name"}, help = "Epic name", defaultValue = ShellOption.NULL) String name,
            @ShellOption(value = {"-d", "--desc"}, help = "Epic description", defaultValue = ShellOption.NULL) String description,
            @ShellOption(value = {"-sp", "--story-points"}, help = "Story points", defaultValue = "0") Integer storyPoints,
            @ShellOption(value = {"-s", "--start-date"}, help = "Start date", defaultValue = ShellOption.NULL) String startDate,
            @ShellOption(value = {"-te", "--target-end-date"}, help = "Target end date", defaultValue = ShellOption.NULL) String targetEndDate,
            @ShellOption(value = {"-ae", "--actual-end-date"}, help = "Actual end date", defaultValue = ShellOption.NULL) String actualEndDate
    ) {
        try {
            shellService.printHeading("Updating epic...");

            Object currentEpicObj = apiService.get("/epics/" + epicId, Object.class);
            @SuppressWarnings("unchecked")
            Map<String, Object> currentEpic = (Map<String, Object>) currentEpicObj;

            Map<String, Object> updatedEpic = new HashMap<>(currentEpic);

            if (description != null) updatedEpic.put("description", description);
            if (storyPoints != null) updatedEpic.put("storyPoints", storyPoints);
            if (startDate != null) updatedEpic.put("startDate", DateUtils.parseDate(startDate));
            if (targetEndDate != null) updatedEpic.put("targetEndDate", DateUtils.parseDate(targetEndDate));
            if (actualEndDate != null) updatedEpic.put("actualEndDate", DateUtils.parseDate(actualEndDate));

            if (name != null) {
                updatedEpic.put("name", name);
            }

            apiService.put("/epics/" + epicId, updatedEpic, Object.class);
            shellService.printSuccess("Epic updated successfully!");

        } catch (Exception e) {
            shellService.printError("Error updating epic: " + e.getMessage());
        }
    }

    @ShellMethod(key = "epic-delete", value = "Delete an epic")
    @ShellMethodAvailability("isUserLoggedIn")
    public void deleteEpic(@ShellOption(help = "Epic ID") String epicId) {
        try {
            shellService.printHeading("Reassigning tasks before deleting epic...");

            Object[] tasks = apiService.get("/tasks?epicId=" + epicId, Object[].class);

            if (tasks.length > 0) {
                for (Object taskObj : tasks) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> task = (Map<String, Object>) taskObj;

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("epicId", null);

                    apiService.patch("/tasks/" + task.get("id") + "/remove-epic", updates, Void.class);
                }
                shellService.printInfo("All associated tasks have been unlinked from the epic.");
            } else {
                shellService.printInfo("No tasks linked to this epic.");
            }

            shellService.printHeading("Deleting epic...");
            apiService.delete("/epics/" + epicId, Void.class);
            shellService.printSuccess("Epic deleted successfully!");

        } catch (Exception e) {
            shellService.printError("Error deleting epic: " + e.getMessage());
        }
    }

    public Availability isUserLoggedIn() {
        return userSession.isAuthenticated()
                ? Availability.available()
                : Availability.unavailable("you are not logged in. Please use 'login' command first");
    }
}
