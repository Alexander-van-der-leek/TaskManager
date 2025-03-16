package com.taskmanagement.cli.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanagement.cli.config.UserSession;
import com.taskmanagement.cli.service.APIService;
import com.taskmanagement.cli.service.ShellService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.shell.Availability;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.web.client.RestTemplate;


import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.*;

@ShellComponent
public class EpicSprintShellCommands {

    @Autowired
    private UserSession userSession;

    @Autowired
    private ShellService shellService;

    @Autowired
    private APIService apiService;

    /* Epic commands */

    @ShellMethod(key = "epic-list", value = "List all epics")
    @ShellMethodAvailability("isUserLoggedIn")
    public void listEpics() {
        shellService.printInfo("Feature coming soon: List of epics will be displayed here.");
        shellService.printInfo("This feature is not yet implemented.");
    }

    @ShellMethod(key = "epic-create", value = "Create a new epic")
    @ShellMethodAvailability("isUserLoggedIn")
    public void createEpic() {
        shellService.printInfo("Feature coming soon: Create epic functionality will be added here.");
        shellService.printInfo("This feature is not yet implemented.");
    }

    /* Sprint commands */

    @ShellMethod(key = "sprint-list", value = "List all sprints")
    @ShellMethodAvailability("isUserLoggedIn")
    public void listSprints() {
        shellService.printInfo("Feature coming soon: List of sprints will be displayed here.");
        shellService.printInfo("This feature is not yet implemented.");
    }

    @ShellMethod(key = "sprint-create", value = "Create a new sprint")
    @ShellMethodAvailability("isUserLoggedIn")
    public void createSprint() {
        if (!userSession.isAuthenticated()) {
            shellService.printError("You need to log in before creating a sprint.");
            return;
        }

        Scanner scanner = new Scanner(System.in);

        shellService.printInfo("Enter Scrum Master's name:");
        String scrumMasterName = scanner.nextLine().trim();

        UUID scrumMasterId = fetchUserIdByName(scrumMasterName);
        if (scrumMasterId == null) {
            shellService.printError("User not found. Please check the name and try again.");
            return;
        }
        shellService.printInfo("Scrum Master ID found: " + scrumMasterId);

        shellService.printInfo("Enter sprint name:");
        String name = scanner.nextLine().trim();

        shellService.printInfo("Enter sprint goal:");
        String goal = scanner.nextLine().trim();

        shellService.printInfo("Enter sprint capacity points:");
        int capacityPoints = Integer.parseInt(scanner.nextLine().trim());

        shellService.printInfo("Enter sprint start date (yyyy-MM-dd):");
        String startDate = scanner.nextLine().trim();

        shellService.printInfo("Enter sprint end date (yyyy-MM-dd):");
        String endDate = scanner.nextLine().trim();

        System.out.println("Token: " + userSession.getToken());  // Debugging line

        Map<String, Object> sprintData = new HashMap<>();
        sprintData.put("name", name);
        sprintData.put("goal", goal);
        sprintData.put("capacityPoints", capacityPoints);
        sprintData.put("startDate", startDate);
        sprintData.put("endDate", endDate);
        sprintData.put("scrumMasterId", scrumMasterId.toString());
        sprintData.put("active", true);

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

    private UUID fetchUserIdByName(String name) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String apiUrl = "http://localhost:8080/api/users/find?name=" + URLEncoder.encode(name, StandardCharsets.UTF_8);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + userSession.getToken()); // Send JWT token
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(apiUrl, HttpMethod.GET, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return UUID.fromString((String) response.getBody().get("userId"));
            }
        } catch (Exception e) {
            shellService.printError("Error fetching user ID: " + e.getMessage());
        }
        return null;
    }


    @ShellMethod(key = "list-users", value = "List all users in the system")
    @ShellMethodAvailability("isUserLoggedIn")
    public void listUsers() {
        if (!userSession.isAuthenticated()) {
            shellService.printError("You need to log in before listing users.");
            return;
        }

        try {
            String jwtToken = userSession.getToken(); // Retrieve the stored JWT token
            if (jwtToken == null || jwtToken.isEmpty()) {
                shellService.printError("Authentication token not found. Please log in again.");
                return;
            }

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + jwtToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(headers);
            String apiUrl = "http://localhost:8080/api/users/all"; // Adjust URL if necessary

            ResponseEntity<List> response = restTemplate.exchange(apiUrl, HttpMethod.GET, entity, List.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<Map<String, Object>> users = response.getBody();
                shellService.printInfo("List of Users:");
                for (Map<String, Object> user : users) {
                    shellService.printInfo("ID: " + user.get("id") +
                            " | Name: " + user.get("name") +
                            " | Email: " + user.get("email"));
                }
            } else {
                shellService.printError("Failed to fetch users. HTTP Status: " + response.getStatusCode());
            }
        } catch (Exception e) {
            shellService.printError("Error retrieving users: " + e.getMessage());
        }
    }

    @ShellMethod(key = "sprint-start", value = "Start a sprint")
    @ShellMethodAvailability("isUserLoggedIn")
    public void startSprint() {
        try {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter the sprint name: ");
            String sprintName = scanner.nextLine();

            UUID sprintId = fetchSprintIdByName(sprintName);

            if (sprintId == null) {
                System.out.println("Sprint not found. Operation aborted.");
                return;
            }

            String token = userSession.getToken();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            headers.setContentType(MediaType.APPLICATION_JSON);

            RestTemplate restTemplate = new RestTemplate();
            String apiUrl = "http://localhost:8080/api/sprints/" + sprintId + "/start";

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("Sprint with ID " + sprintId + " has been started.");
            } else {
                System.out.println("Failed to start sprint. HTTP Status: " + response.getStatusCode());
            }
        } catch (Exception e) {
            System.out.println("Error starting sprint: " + e.getMessage());
        }
    }

    @ShellMethod(key = "sprint-end", value = "End a sprint")
    @ShellMethodAvailability("isUserLoggedIn")
    public void endSprint() {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Enter the sprint name: ");
            String sprintName = scanner.nextLine().trim();

            UUID sprintId = fetchSprintIdByName(sprintName);
            if (sprintId == null) {
                System.out.println("Sprint not found with name: " + sprintName);
                return;
            }

            sendSprintRequest(sprintId, "end");
        } catch (Exception e) {
            System.out.println("Error ending sprint: " + e.getMessage());
        }
    }
    private UUID fetchSprintIdByName(String sprintName) {
        try {
            String token = userSession.getToken();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            headers.setContentType(MediaType.APPLICATION_JSON);

            RestTemplate restTemplate = new RestTemplate();
            String apiUrl = "http://localhost:8080/api/sprints/by-name?sprintName=" + sprintName;

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.exchange(apiUrl, HttpMethod.GET, entity, Map.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null || !response.getBody().containsKey("id")) {
                shellService.printError("Failed to fetch sprint ID. API returned: " + response.getStatusCode());
                return null;
            }

            return UUID.fromString(response.getBody().get("id").toString());
        } catch (Exception e) {
            shellService.printError("Error fetching sprint ID: " + e.getMessage());
            return null;
        }
    }

    private void sendSprintRequest(UUID sprintId, String action) {
        try {
            String token = userSession.getToken();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            headers.setContentType(MediaType.APPLICATION_JSON);

            RestTemplate restTemplate = new RestTemplate();
            String apiUrl = "http://localhost:8080/api/sprints/" + sprintId + "/" + action;

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("Sprint with ID " + sprintId + " has been " + (action.equals("start") ? "started" : "ended") + ".");
            } else {
                System.out.println("Failed to " + action + " sprint. HTTP Status: " + response.getStatusCode());
            }
        } catch (Exception e) {
            System.out.println("Error " + action + "ing sprint: " + e.getMessage());
        }
    }


    public Availability isUserLoggedIn() {
        return userSession.isAuthenticated()
                ? Availability.available()
                : Availability.unavailable("you are not logged in. Please use 'login' command first");
    }
}