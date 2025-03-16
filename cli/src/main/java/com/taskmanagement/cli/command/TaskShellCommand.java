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

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ShellComponent
public class TaskShellCommand {

    @Autowired
    private APIService apiService;

    @Autowired
    private UserSession userSession;

    @Autowired
    private ShellService shellService;

    @ShellMethod(key = "task-list", value = "List all tasks")
    @ShellMethodAvailability("isUserLoggedIn")
    public void listTasks() {
        try {
            shellService.printHeading("Fetching Tasks...");

            Object[] tasks = apiService.get("/tasks", Object[].class);
            if (tasks.length == 0) {
                shellService.printInfo("No tasks found");
            } else {
                displayTasksTable(tasks);
            }
        } catch (Exception e) {
            shellService.printError("Error fetching tasks: " + e.getMessage());
        }
    }

    @ShellMethod(key = "task-create", value = "Create a new task")
    @ShellMethodAvailability("isUserLoggedIn")
    public void createTask(
            @ShellOption(value = {"-t", "--title"}, help = "Task title") String title,
            @ShellOption(value = {"-d", "--desc"}, help = "Task description") String description,
            @ShellOption(value = {"-a", "--assignee"}, help = "Assignee name") String assigneeName,
            @ShellOption(value = {"-s", "--status"}, help = "Status ID") Integer statusId,
            @ShellOption(value = {"-p", "--priority"}, help = "Priority ID") Integer priorityId,
            @ShellOption(value = {"-due", "--due-date"}, help = "Due date (YYYY-MM-DD)") String dueDate,
            @ShellOption(value = {"-e", "--epic"}, help = "Epic ID", defaultValue = ShellOption.NULL) Integer epicId,
            @ShellOption(value = {"-sp", "--sprint"}, help = "Sprint ID", defaultValue = ShellOption.NULL) Integer sprintId,
            @ShellOption(value = {"-pts", "--story-points"}, help = "Story points", defaultValue = "0") Integer storyPoints,
            @ShellOption(value = {"-hrs", "--estimated-hours"}, help = "Estimated hours", defaultValue = "0") Integer estimatedHours
    ) {
        try {
            shellService.printHeading("Creating new task...");

            Object[] users = apiService.get("/users/search?name=" + assigneeName, Object[].class);

            if (users.length == 0) {
                shellService.printError("No user found with name containing: " + assigneeName);
                return;
            }

            if (users.length > 1) {
                shellService.printWarning("Multiple users found with that name. Please be more specific:");
                displayUsersTable(users);
                return;
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> user = (Map<String, Object>)users[0];
            String assigneeId = String.valueOf(user.get("id"));

            Map<String, Object> task = new HashMap<>();
            task.put("title", title);
            task.put("description", description);
            task.put("assignedToId", assigneeId);
            task.put("statusId", statusId);
            task.put("priorityId", priorityId);
            task.put("dueDate", DateUtils.parseDate(dueDate));
            task.put("storyPoints", storyPoints);
            task.put("estimatedHours", estimatedHours);

            if (epicId != null) {
                task.put("epicId", epicId);
            }

            if (sprintId != null) {
                task.put("sprintId", sprintId);
            }

            Object createdTask = apiService.post("/tasks", task, Object.class);
            shellService.printSuccess("Task created successfully!");

            @SuppressWarnings("unchecked")
            Map<String, Object> taskResult = (Map<String, Object>) createdTask;
            shellService.printInfo("ID: " + taskResult.get("id"));
            shellService.printInfo("Title: " + taskResult.get("title"));
            shellService.printInfo("Assigned to: " + taskResult.get("assignedToName"));

        } catch (Exception e) {
            shellService.printError("Error creating task: " + e.getMessage());
        }
    }

    @ShellMethod(key = "task-get", value = "Get task details")
    @ShellMethodAvailability("isUserLoggedIn")
    public void getTask(@ShellOption(help = "Task ID") String taskId) {
        try {
            shellService.printHeading("Fetching task details...");

            Object taskObj = apiService.get("/tasks/" + taskId, Object.class);
            @SuppressWarnings("unchecked")
            Map<String, Object> task = (Map<String, Object>) taskObj;

            shellService.printHeading("Task Details:");
            shellService.printInfo("ID: " + task.get("id"));
            shellService.printInfo("Title: " + task.get("title"));
            shellService.printInfo("Description: " + task.get("description"));
            shellService.printInfo("Assigned to: " + task.get("assignedToName"));
            shellService.printInfo("Status: " + task.get("statusName"));
            shellService.printInfo("Priority: " + task.get("priorityName"));
            shellService.printInfo("Story Points: " + task.get("storyPoints"));
            shellService.printInfo("Estimated Hours: " + task.get("estimatedHours"));
            shellService.printInfo("Due Date: " + task.get("dueDate"));

            if (task.get("epicName") != null) {
                shellService.printInfo("Epic: " + task.get("epicName") + " (" + task.get("epicId") + ")");
            }

            if (task.get("sprintName") != null) {
                shellService.printInfo("Sprint: " + task.get("sprintName") + " (" + task.get("sprintId") + ")");
            }
        } catch (Exception e) {
            shellService.printError("Error fetching task: " + e.getMessage());
        }
    }

    @ShellMethod(key = "task-update", value = "Update a task")
    @ShellMethodAvailability("isUserLoggedIn")
    public void updateTask(
            @ShellOption(help = "Task ID") String taskId,
            @ShellOption(value = {"-t", "--title"}, help = "Task title", defaultValue = ShellOption.NULL) String title,
            @ShellOption(value = {"-d", "--desc"}, help = "Task description", defaultValue = ShellOption.NULL) String description,
            @ShellOption(value = {"-a", "--assignee"}, help = "Assignee name", defaultValue = ShellOption.NULL) String assigneeName,
            @ShellOption(value = {"-s", "--status"}, help = "Status ID", defaultValue = ShellOption.NULL) Integer statusId,
            @ShellOption(value = {"-p", "--priority"}, help = "Priority ID", defaultValue = ShellOption.NULL) Integer priorityId,
            @ShellOption(value = {"-due", "--due-date"}, help = "Due date (YYYY-MM-DD)", defaultValue = ShellOption.NULL) String dueDate,
            @ShellOption(value = {"-e", "--epic"}, help = "Epic ID", defaultValue = ShellOption.NULL) Integer epicId,
            @ShellOption(value = {"-sp", "--sprint"}, help = "Sprint ID", defaultValue = ShellOption.NULL) Integer sprintId,
            @ShellOption(value = {"-pts", "--story-points"}, help = "Story points", defaultValue = ShellOption.NULL) Integer storyPoints,
            @ShellOption(value = {"-hrs", "--estimated-hours"}, help = "Estimated hours", defaultValue = ShellOption.NULL) Integer estimatedHours
    ) {
        try {
            shellService.printHeading("Updating task...");

            Object currentTaskObj = apiService.get("/tasks/" + taskId, Object.class);
            @SuppressWarnings("unchecked")
            Map<String, Object> currentTask = (Map<String, Object>) currentTaskObj;

            Map<String, Object> updatedTask = new HashMap<>(currentTask);
            if (title != null) updatedTask.put("title", title);
            if (description != null) updatedTask.put("description", description);

            if (assigneeName != null) {
                Object[] users = apiService.get("/users/search?name=" + assigneeName, Object[].class);

                if (users.length == 0) {
                    shellService.printError("No user found with name containing: " + assigneeName);
                    return;
                }

                if (users.length > 1) {
                    shellService.printWarning("Multiple users found with that name. Please be more specific:");
                    displayUsersTable(users);
                    return;
                }

                @SuppressWarnings("unchecked")
                Map<String, Object> user = (Map<String, Object>)users[0];
                String assigneeId = String.valueOf(user.get("id"));
                updatedTask.put("assignedToId", assigneeId);
            }

            if (statusId != null) updatedTask.put("statusId", statusId);
            if (priorityId != null) updatedTask.put("priorityId", priorityId);
            if (dueDate != null) updatedTask.put("dueDate", DateUtils.parseDate(dueDate));
            if (epicId != null) updatedTask.put("epicId", epicId);
            if (sprintId != null) updatedTask.put("sprintId", sprintId);
            if (storyPoints != null) updatedTask.put("storyPoints", storyPoints);
            if (estimatedHours != null) updatedTask.put("estimatedHours", estimatedHours);

            if (updatedTask.get("dueDate") instanceof String) {
                updatedTask.put("dueDate", DateUtils.parseDate((String)updatedTask.get("dueDate")));
            }

            apiService.put("/tasks/" + taskId, updatedTask, Object.class);
            shellService.printSuccess("Task updated successfully!");

        } catch (Exception e) {
            shellService.printError("Error updating task: " + e.getMessage());
        }
    }

    @ShellMethod(key = "task-change-status", value = "Change task status")
    @ShellMethodAvailability("isUserLoggedIn")
    public void changeTaskStatus(
            @ShellOption(help = "Task ID") String taskId,
            @ShellOption(help = "Status ID") String statusId
    ) {
        try {
            shellService.printHeading("Changing task status...");

            Object updatedTask = apiService.patch("/tasks/" + taskId + "/status/" + statusId, null, Object.class);
            shellService.printSuccess("Task status changed successfully!");

            @SuppressWarnings("unchecked")
            Map<String, Object> taskResult = (Map<String, Object>) updatedTask;
            shellService.printInfo("New Status: " + taskResult.get("statusName"));

        } catch (Exception e) {
            shellService.printError("Error changing task status: " + e.getMessage());
        }
    }

    @ShellMethod(key = "task-assign", value = "Assign task to user")
    @ShellMethodAvailability("isUserLoggedIn")
    public void assignTask(
            @ShellOption(help = "Task ID") String taskId,
            @ShellOption(help = "Assignee name") String assigneeName
    ) {
        try {
            shellService.printHeading("Assigning task...");

            Object[] users = apiService.get("/users/search?name=" + assigneeName, Object[].class);

            if (users.length == 0) {
                shellService.printError("No user found with name containing: " + assigneeName);
                return;
            }

            if (users.length > 1) {
                shellService.printWarning("Multiple users found with that name. Please be more specific:");
                displayUsersTable(users);
                return;
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> user = (Map<String, Object>)users[0];
            String assigneeId = String.valueOf(user.get("id"));

            Object updatedTask = apiService.patch("/tasks/" + taskId + "/assign/" + assigneeId, null, Object.class);
            shellService.printSuccess("Task assigned successfully!");

            @SuppressWarnings("unchecked")
            Map<String, Object> taskResult = (Map<String, Object>) updatedTask;
            shellService.printInfo("Assigned to: " + taskResult.get("assignedToName"));

        } catch (Exception e) {
            shellService.printError("Error assigning task: " + e.getMessage());
        }
    }

    @ShellMethod(key = "task-add-to-sprint", value = "Add task to sprint")
    @ShellMethodAvailability("isUserLoggedIn")
    public void addTaskToSprint(
            @ShellOption(help = "Task ID") String taskId,
            @ShellOption(help = "Sprint ID") String sprintId
    ) {
        try {
            shellService.printHeading("Adding task to sprint...");

            Object updatedTask = apiService.patch("/tasks/" + taskId + "/add-to-sprint/" + sprintId, null, Object.class);
            shellService.printSuccess("Task added to sprint successfully!");

            @SuppressWarnings("unchecked")
            Map<String, Object> taskResult = (Map<String, Object>) updatedTask;
            shellService.printInfo("Added to sprint: " + taskResult.get("sprintName"));

        } catch (Exception e) {
            shellService.printError("Error adding task to sprint: " + e.getMessage());
        }
    }

    @ShellMethod(key = "task-remove-from-sprint", value = "Remove task from sprint")
    @ShellMethodAvailability("isUserLoggedIn")
    public void removeTaskFromSprint(
            @ShellOption(help = "Task ID") String taskId
    ) {
        try {
            shellService.printHeading("Removing task from sprint...");

            Object updatedTask = apiService.patch("/tasks/" + taskId + "/remove-from-sprint", null, Object.class);
            shellService.printSuccess("Task removed from sprint successfully!");

        } catch (Exception e) {
            shellService.printError("Error removing task from sprint: " + e.getMessage());
        }
    }

    @ShellMethod(key = "task-add-to-epic", value = "Add task to epic")
    @ShellMethodAvailability("isUserLoggedIn")
    public void addTaskToEpic(
            @ShellOption(help = "Task ID") String taskId,
            @ShellOption(help = "Epic ID") String epicId
    ) {
        try {
            shellService.printHeading("Adding task to epic...");

            Object updatedTask = apiService.patch("/tasks/" + taskId + "/add-to-epic/" + epicId, null, Object.class);
            shellService.printSuccess("Task added to epic successfully!");

            @SuppressWarnings("unchecked")
            Map<String, Object> taskResult = (Map<String, Object>) updatedTask;
            shellService.printInfo("Added to epic: " + taskResult.get("epicName"));

        } catch (Exception e) {
            shellService.printError("Error adding task to epic: " + e.getMessage());
        }
    }

    @ShellMethod(key = "task-remove-from-epic", value = "Remove task from epic")
    @ShellMethodAvailability("isUserLoggedIn")
    public void removeTaskFromEpic(
            @ShellOption(help = "Task ID") String taskId
    ) {
        try {
            shellService.printHeading("Removing task from epic...");

            Object updatedTask = apiService.patch("/tasks/" + taskId + "/remove-from-epic", null, Object.class);
            shellService.printSuccess("Task removed from epic successfully!");

        } catch (Exception e) {
            shellService.printError("Error removing task from epic: " + e.getMessage());
        }
    }

    @ShellMethod(key = "task-filter", value = "Filter tasks")
    @ShellMethodAvailability("isUserLoggedIn")
    public void filterTasks(
            @ShellOption(value = {"-a", "--assignee"}, help = "Assignee name", defaultValue = ShellOption.NULL) String assigneeName,
            @ShellOption(value = {"-s", "--status"}, help = "Status ID", defaultValue = ShellOption.NULL) Integer statusId,
            @ShellOption(value = {"-p", "--priority"}, help = "Priority ID", defaultValue = ShellOption.NULL) Integer priorityId,
            @ShellOption(value = {"-sp", "--sprint"}, help = "Sprint ID", defaultValue = ShellOption.NULL) Integer sprintId,
            @ShellOption(value = {"-e", "--epic"}, help = "Epic ID", defaultValue = ShellOption.NULL) Integer epicId
    ) {
        try {
            shellService.printHeading("Filtering tasks...");

            Map<String, Object> filterParams = new HashMap<>();

            if (assigneeName != null) {
                Object[] users = apiService.get("/users/search?name=" + assigneeName, Object[].class);

                if (users.length == 0) {
                    shellService.printError("No user found with name containing: " + assigneeName);
                    return;
                }

                if (users.length > 1) {
                    shellService.printWarning("Multiple users found with that name. Please be more specific:");
                    displayUsersTable(users);
                    return;
                }

                @SuppressWarnings("unchecked")
                Map<String, Object> user = (Map<String, Object>)users[0];
                String assigneeId = String.valueOf(user.get("id"));
                filterParams.put("assignedToId", assigneeId);
            }

            if (statusId != null) filterParams.put("statusId", statusId);
            if (priorityId != null) filterParams.put("priorityId", priorityId);
            if (sprintId != null) filterParams.put("sprintId", sprintId);
            if (epicId != null) filterParams.put("epicId", epicId);

            Object[] tasks = apiService.post("/tasks/filter", filterParams, Object[].class);

            if (tasks.length == 0) {
                shellService.printInfo("No tasks found matching the filter criteria");
            } else {
                displayTasksTable(tasks);
            }

        } catch (Exception e) {
            shellService.printError("Error filtering tasks: " + e.getMessage());
        }
    }

    @ShellMethod(key = "task-my", value = "List my active tasks")
    @ShellMethodAvailability("isUserLoggedIn")
    public void getMyTasks() {
        try {
            shellService.printHeading("Fetching your active tasks...");

            Object[] tasks = apiService.get("/tasks/my-tasks", Object[].class);

            if (tasks.length == 0) {
                shellService.printInfo("You have no active tasks");
            } else {
                displayTasksTable(tasks);
            }
        } catch (Exception e) {
            shellService.printError("Error fetching your tasks: " + e.getMessage());
        }
    }

    @ShellMethod(key = "task-delete", value = "Delete a task")
    @ShellMethodAvailability("isUserLoggedIn")
    public void deleteTask(@ShellOption(help = "Task ID") String taskId) {
        try {
            shellService.printHeading("Deleting task...");
            apiService.delete("/tasks/" + taskId, Object.class);
            shellService.printSuccess("Task deleted successfully!");
        } catch (Exception e) {
            shellService.printError("Error deleting task: " + e.getMessage());
        }
    }

    @ShellMethod(key = "task-overdue", value = "View your overdue tasks")
    @ShellMethodAvailability("isUserLoggedIn")
    public void overdueTasks() {
        try {
            shellService.printHeading("Getting Overdue tasks....");
            Object[] tasks = apiService.get("/tasks/overdue", Object[].class);

            if (tasks.length == 0) {
                shellService.printInfo("You have no overdue tasks");
            } else {
                displayTasksTable(tasks);
            }
        } catch (Exception e) {
            shellService.printError("Error getting overdue tasks: " + e.getMessage());
        }
    }

    @ShellMethod(key = "task-epic", value = "List tasks for an epic")
    @ShellMethodAvailability("isUserLoggedIn")
    public void listTasksByEpic(@ShellOption(help = "Epic ID") String epicId) {
        try {
            shellService.printHeading("Fetching tasks for epic: " + epicId);

            Object[] tasks = apiService.get("/tasks/epic/" + epicId, Object[].class);
            if (tasks.length == 0) {
                shellService.printInfo("No tasks found for this epic");
            } else {
                displayTasksTable(tasks);
            }
        } catch (Exception e) {
            shellService.printError("Error fetching tasks: " + e.getMessage());
        }
    }

    @ShellMethod(key = "task-sprint", value = "List tasks for a sprint")
    @ShellMethodAvailability("isUserLoggedIn")
    public void listTasksBySprint(@ShellOption(help = "Sprint ID") String sprintId) {
        try {
            shellService.printHeading("Fetching tasks for sprint: " + sprintId);

            Object[] tasks = apiService.get("/tasks/sprint/" + sprintId, Object[].class);
            if (tasks.length == 0) {
                shellService.printInfo("No tasks found for this sprint");
            } else {
                displayTasksTable(tasks);
            }
        } catch (Exception e) {
            shellService.printError("Error fetching tasks: " + e.getMessage());
        }
    }

    @ShellMethod(key = "task-recent", value = "List recently updated tasks")
    @ShellMethodAvailability("isUserLoggedIn")
    public void listRecentTasks(
            @ShellOption(value = {"-h", "--hours"}, help = "Hours ago (default: 24)", defaultValue = "24") Integer hours) {
        try {
            shellService.printHeading("Fetching tasks updated in the last " + hours + " hours...");

            Object[] tasks = apiService.get("/tasks/recent?hours=" + hours, Object[].class);
            if (tasks.length == 0) {
                shellService.printInfo("No tasks updated in the last " + hours + " hours");
            } else {
                displayTasksTable(tasks);
            }
        } catch (Exception e) {
            shellService.printError("Error fetching tasks: " + e.getMessage());
        }
    }

    @ShellMethod(key = "sprint-stats", value = "Show statistics for a sprint")
    @ShellMethodAvailability("isUserLoggedIn")
    public void getSprintStats(@ShellOption(help = "Sprint ID") String sprintId) {
        try {
            shellService.printHeading("Fetching statistics for sprint: " + sprintId);

            @SuppressWarnings("unchecked")
            Map<String, Long> stats = apiService.get("/tasks/sprint/" + sprintId + "/stats", Map.class);

            if (stats.isEmpty()) {
                shellService.printInfo("No statistics available for this sprint");
            } else {
                // Display stats in a nice format
                shellService.printHeading("Sprint Status Breakdown:");
                for (Map.Entry<String, Long> entry : stats.entrySet()) {
                    shellService.printInfo(String.format("%-15s: %d", entry.getKey(), entry.getValue()));
                }
            }
        } catch (Exception e) {
            shellService.printError("Error fetching sprint statistics: " + e.getMessage());
        }
    }

    @ShellMethod(key = "status-list", value = "List all available task statuses")
    @ShellMethodAvailability("isUserLoggedIn")
    public void listStatuses() {
        try {
            shellService.printHeading("Fetching Available Statuses...");

            Object[] statuses = apiService.get("/tasks/statuses", Object[].class);
            if (statuses.length == 0) {
                shellService.printInfo("No statuses found");
            } else {
                List<String[]> tableData = new ArrayList<>();

                for (Object statusObj : statuses) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> status = (Map<String, Object>) statusObj;

                    String[] row = new String[3];
                    row[0] = String.valueOf(status.get("id"));
                    row[1] = String.valueOf(status.get("name"));
                    row[2] = String.valueOf(status.get("displayOrder"));

                    tableData.add(row);
                }

                String[] headers = {"ID", "Name", "Display Order"};
                shellService.printTable(headers, tableData.toArray(new String[0][]));
            }
        } catch (Exception e) {
            shellService.printError("Error fetching statuses: " + e.getMessage());
        }
    }

    @ShellMethod(key = "priority-list", value = "List all available task priorities")
    @ShellMethodAvailability("isUserLoggedIn")
    public void listPriorities() {
        try {
            shellService.printHeading("Fetching Available Priorities...");

            Object[] priorities = apiService.get("/tasks/priorities", Object[].class);
            if (priorities.length == 0) {
                shellService.printInfo("No priorities found");
            } else {
                List<String[]> tableData = new ArrayList<>();

                for (Object priorityObj : priorities) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> priority = (Map<String, Object>) priorityObj;

                    String[] row = new String[3];
                    row[0] = String.valueOf(priority.get("id"));
                    row[1] = String.valueOf(priority.get("name"));
                    row[2] = String.valueOf(priority.get("value"));

                    tableData.add(row);
                }

                String[] headers = {"ID", "Name", "Value"};
                shellService.printTable(headers, tableData.toArray(new String[0][]));
            }
        } catch (Exception e) {
            shellService.printError("Error fetching priorities: " + e.getMessage());
        }
    }

    private void displayTasksTable(Object[] tasks) {
        List<String[]> tableData = new ArrayList<>();

        for (Object taskObj : tasks) {
            @SuppressWarnings("unchecked")
            Map<String, Object> task = (Map<String, Object>) taskObj;

            String[] row = new String[5];
            row[0] = String.valueOf(task.get("id"));
            row[1] = String.valueOf(task.get("title"));
            row[2] = String.valueOf(task.get("assignedToName"));
            row[3] = String.valueOf(task.get("statusName"));
            row[4] = String.valueOf(task.get("priorityName"));

            tableData.add(row);
        }

        String[] headers = {"ID", "Title", "Assigned To", "Status", "Priority"};
        shellService.printTable(headers, tableData.toArray(new String[0][]));
    }

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