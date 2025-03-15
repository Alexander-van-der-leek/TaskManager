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

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ShellComponent
public class TaskCommentShellCommand {

    @Autowired
    private APIService apiService;

    @Autowired
    private UserSession userSession;

    @Autowired
    private ShellService shellService;

    @ShellMethod(key = "comment-list", value = "List comments for a task")
    @ShellMethodAvailability("isUserLoggedIn")
    public void listComments(@ShellOption(help = "Task ID") String taskId) {
        try {
            shellService.printHeading("Fetching comments for task: " + taskId);

            Object[] comments = apiService.get("/comments/task/" + taskId, Object[].class);
            if (comments.length == 0) {
                shellService.printInfo("No comments found for this task");
            } else {
                List<String[]> tableData = new ArrayList<>();

                for (Object commentObj : comments) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> comment = (Map<String, Object>) commentObj;

                    String createdAt = "";
                    if (comment.get("createdAt") != null) {
                        try {
                            ZonedDateTime dateTime = ZonedDateTime.parse(comment.get("createdAt").toString());
                            createdAt = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                        } catch (Exception e) {
                            createdAt = comment.get("createdAt").toString();
                        }
                    }

                    String[] row = new String[4];
                    row[0] = String.valueOf(comment.get("id"));
                    row[1] = String.valueOf(comment.get("userName"));
                    row[2] = createdAt;
                    row[3] = String.valueOf(comment.get("content"));

                    tableData.add(row);
                }

                String[] headers = {"ID", "Author", "Created At", "Content"};
                shellService.printTable(headers, tableData.toArray(new String[0][]));
            }
        } catch (Exception e) {
            shellService.printError("Error fetching comments: " + e.getMessage());
        }
    }

    @ShellMethod(key = "comment-add", value = "Add a comment to a task")
    @ShellMethodAvailability("isUserLoggedIn")
    public void addComment(
            @ShellOption(help = "Task ID") String taskId,
            @ShellOption(help = "Comment content") String content) {
        try {
            shellService.printHeading("Adding comment to task: " + taskId);

            Map<String, Object> comment = new HashMap<>();
            comment.put("taskId", Integer.parseInt(taskId));
            comment.put("content", content);

            Object createdComment = apiService.post("/comments", comment, Object.class);
            shellService.printSuccess("Comment added successfully!");

            @SuppressWarnings("unchecked")
            Map<String, Object> commentResult = (Map<String, Object>) createdComment;
            shellService.printInfo("ID: " + commentResult.get("id"));
            shellService.printInfo("Content: " + commentResult.get("content"));

        } catch (Exception e) {
            shellService.printError("Error adding comment: " + e.getMessage());
        }
    }

    @ShellMethod(key = "comment-update", value = "Update a comment")
    @ShellMethodAvailability("isUserLoggedIn")
    public void updateComment(
            @ShellOption(help = "Comment ID") String commentId,
            @ShellOption(help = "New content") String content) {
        try {
            shellService.printHeading("Updating comment: " + commentId);

            Map<String, Object> comment = new HashMap<>();
            comment.put("content", content);

            Object updatedComment = apiService.put("/comments/" + commentId, comment, Object.class);
            shellService.printSuccess("Comment updated successfully!");

            @SuppressWarnings("unchecked")
            Map<String, Object> commentResult = (Map<String, Object>) updatedComment;
            shellService.printInfo("ID: " + commentResult.get("id"));
            shellService.printInfo("Content: " + commentResult.get("content"));

        } catch (Exception e) {
            shellService.printError("Error updating comment: " + e.getMessage());
        }
    }

    @ShellMethod(key = "comment-delete", value = "Delete a comment")
    @ShellMethodAvailability("isUserLoggedIn")
    public void deleteComment(@ShellOption(help = "Comment ID") String commentId) {
        try {
            shellService.printHeading("Deleting comment: " + commentId);
            apiService.delete("/comments/" + commentId, Object.class);
            shellService.printSuccess("Comment deleted successfully!");
        } catch (Exception e) {
            shellService.printError("Error deleting comment: " + e.getMessage());
        }
    }

    public Availability isUserLoggedIn() {
        return userSession.isAuthenticated()
                ? Availability.available()
                : Availability.unavailable("you are not logged in. Please use 'login' command first");
    }
}