package com.taskmanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanagement.dto.TaskDTO;
import com.taskmanagement.dto.TaskFilterDTO;
import com.taskmanagement.security.JWTFilter;
import com.taskmanagement.security.JWTTokenProvider;
import com.taskmanagement.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.ZonedDateTime;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
public class TaskControllerTest {

    @MockBean
    private JWTTokenProvider jwtTokenProvider;

    @MockBean
    private JWTFilter jwtFilter;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID userId;
    private TaskDTO taskDTO;
    private List<TaskDTO> taskDTOList;
    private User userDetails;

    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    void setUp() {
        userId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

        taskDTO = new TaskDTO();
        taskDTO.setId(1);
        taskDTO.setTitle("Test Task");
        taskDTO.setDescription("Test Description");
        taskDTO.setAssignedToId(UUID.randomUUID());
        taskDTO.setAssignedToName("Test Assignee");
        taskDTO.setStatusId(1);
        taskDTO.setStatusName("IN_PROGRESS");
        taskDTO.setPriorityId(1);
        taskDTO.setPriorityName("HIGH");
        taskDTO.setStoryPoints(5);
        taskDTO.setEstimatedHours(8);
        taskDTO.setDueDate(ZonedDateTime.now().plusDays(7));

        taskDTOList = Arrays.asList(taskDTO);

        Collection<SimpleGrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_ADMIN")
        );
        userDetails = new User(userId.toString(), "", authorities);

        when(jwtTokenProvider.validateToken(any())).thenReturn(true);
        when(jwtTokenProvider.getAuthentication(any())).thenReturn(
                new UsernamePasswordAuthenticationToken(userDetails, "", authorities)
        );

        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000", roles = {"ADMIN"})
    void getTasksByAssignee_ShouldReturnAssigneeTasks() throws Exception {

        UUID assigneeId = UUID.randomUUID();
        when(taskService.getTasksByAssignee(assigneeId, userId)).thenReturn(taskDTOList);

        mockMvc.perform(get("/api/tasks/assignee/" + assigneeId)
                        .with(SecurityMockMvcRequestPostProcessors.user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].title", is("Test Task")));
    }

    @Test
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000", roles = {"ADMIN"})
    void getTasksByEpic_ShouldReturnEpicTasks() throws Exception {
        UUID epicId = UUID.randomUUID();
        when(taskService.getTasksByEpic(epicId, userId)).thenReturn(taskDTOList);

        mockMvc.perform(get("/api/tasks/epic/" + epicId)
                        .with(SecurityMockMvcRequestPostProcessors.user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].title", is("Test Task")));
    }

    @Test
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000", roles = {"ADMIN"})
    void getTasksBySprint_ShouldReturnSprintTasks() throws Exception {

        UUID sprintId = UUID.randomUUID();
        when(taskService.getTasksBySprint(sprintId, userId)).thenReturn(taskDTOList);

        mockMvc.perform(get("/api/tasks/sprint/" + sprintId)
                        .with(SecurityMockMvcRequestPostProcessors.user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].title", is("Test Task")));
    }

    @Test
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000", roles = {"ADMIN"})
    void getSprintStats_ShouldReturnSprintStats() throws Exception {

        UUID sprintId = UUID.randomUUID();
        Map<String, Long> stats = new HashMap<>();
        stats.put("TODO", 5L);
        stats.put("IN_PROGRESS", 3L);
        stats.put("DONE", 2L);

        when(taskService.getSprintStats(sprintId, userId)).thenReturn(stats);

        mockMvc.perform(get("/api/tasks/sprint/" + sprintId + "/stats")
                        .with(SecurityMockMvcRequestPostProcessors.user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.TODO", is(5)))
                .andExpect(jsonPath("$.IN_PROGRESS", is(3)))
                .andExpect(jsonPath("$.DONE", is(2)));
    }

    @Test
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000", roles = {"ADMIN"})
    void getOverdueTasks_ShouldReturnOverdueTasks() throws Exception {

        when(taskService.getOverdueTasks(userId)).thenReturn(taskDTOList);

        mockMvc.perform(get("/api/tasks/overdue")
                        .with(SecurityMockMvcRequestPostProcessors.user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].title", is("Test Task")));
    }

    @Test
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000", roles = {"ADMIN"})
    void getRecentlyUpdatedTasks_ShouldReturnRecentTasks() throws Exception {

        when(taskService.getRecentlyUpdatedTasks(eq(userId), eq(24))).thenReturn(taskDTOList);

        mockMvc.perform(get("/api/tasks/recent")
                        .with(SecurityMockMvcRequestPostProcessors.user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].title", is("Test Task")));
    }

    @Test
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000", roles = {"ADMIN"})
    void changeTaskStatus_ShouldChangeStatus_WhenStatusExists() throws Exception {

        TaskDTO updatedTask = new TaskDTO();
        updatedTask.setId(1);
        updatedTask.setTitle("Test Task");
        updatedTask.setStatusId(2);
        updatedTask.setStatusName("DONE");

        when(taskService.changeTaskStatus(1, 2, userId)).thenReturn(updatedTask);

        mockMvc.perform(patch("/api/tasks/1/status/2")
                        .with(SecurityMockMvcRequestPostProcessors.user(userDetails))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.statusName", is("DONE")));
    }

    @Test
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000", roles = {"ADMIN"})
    void assignTask_ShouldAssignTask_WhenUserExists() throws Exception {

        UUID assigneeId = UUID.randomUUID();
        TaskDTO assignedTask = new TaskDTO();
        assignedTask.setId(1);
        assignedTask.setTitle("Test Task");
        assignedTask.setAssignedToId(assigneeId);
        assignedTask.setAssignedToName("New Assignee");

        when(taskService.assignTask(1, assigneeId, userId)).thenReturn(assignedTask);

        mockMvc.perform(patch("/api/tasks/1/assign/" + assigneeId)
                        .with(SecurityMockMvcRequestPostProcessors.user(userDetails)).
                        with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.assignedToId", is(assigneeId.toString())));
    }

    @Test
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000", roles = {"ADMIN"})
    void addTaskToSprint_ShouldAddTaskToSprint_WhenSprintExists() throws Exception {

        UUID sprintId = UUID.randomUUID();
        TaskDTO updatedTask = new TaskDTO();
        updatedTask.setId(1);
        updatedTask.setTitle("Test Task");
        updatedTask.setSprintId(sprintId);
        updatedTask.setSprintName("Test Sprint");

        when(taskService.addTaskToSprint(1, sprintId, userId)).thenReturn(updatedTask);

        mockMvc.perform(patch("/api/tasks/1/add-to-sprint/" + sprintId)
                        .with(SecurityMockMvcRequestPostProcessors.user(userDetails))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.sprintId", is(sprintId.toString())));
    }

    @Test
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000", roles = {"ADMIN"})
    void removeTaskFromSprint_ShouldRemoveTaskFromSprint() throws Exception {

        TaskDTO updatedTask = new TaskDTO();
        updatedTask.setId(1);
        updatedTask.setTitle("Test Task");
        updatedTask.setSprintId(null);

        when(taskService.removeTaskFromSprint(1, userId)).thenReturn(updatedTask);

        mockMvc.perform(patch("/api/tasks/1/remove-from-sprint")
                        .with(SecurityMockMvcRequestPostProcessors.user(userDetails))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.sprintId").doesNotExist());
    }

    @Test
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000", roles = {"ADMIN"})
    void addTaskToEpic_ShouldAddTaskToEpic_WhenEpicExists() throws Exception {

        UUID epicId = UUID.randomUUID();
        TaskDTO updatedTask = new TaskDTO();
        updatedTask.setId(1);
        updatedTask.setTitle("Test Task");
        updatedTask.setEpicId(epicId);
        updatedTask.setEpicName("Test Epic");

        when(taskService.addTaskToEpic(1, epicId, userId)).thenReturn(updatedTask);

        mockMvc.perform(patch("/api/tasks/1/add-to-epic/" + epicId)
                        .with(SecurityMockMvcRequestPostProcessors.user(userDetails))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.epicId", is(epicId.toString())));
    }

    @Test
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000", roles = {"ADMIN"})
    void removeTaskFromEpic_ShouldRemoveTaskFromEpic() throws Exception {

        TaskDTO updatedTask = new TaskDTO();
        updatedTask.setId(1);
        updatedTask.setTitle("Test Task");
        updatedTask.setEpicId(null);

        when(taskService.removeTaskFromEpic(1, userId)).thenReturn(updatedTask);

        mockMvc.perform(patch("/api/tasks/1/remove-from-epic")
                        .with(SecurityMockMvcRequestPostProcessors.user(userDetails))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.epicId").doesNotExist());
    }

    @Test
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000", roles = {"ADMIN"})
    void deleteTask_ShouldDeleteTask_WhenTaskExists() throws Exception {

        Mockito.doNothing().when(taskService).deleteTask(1, userId);

        mockMvc.perform(delete("/api/tasks/1")
                        .with(SecurityMockMvcRequestPostProcessors.user(userDetails))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000", roles = {"ADMIN"})
    void getTasksByFilter_ShouldReturnFilteredTasks() throws Exception {

        TaskFilterDTO filterDTO = new TaskFilterDTO();
        filterDTO.setStatusId(1);
        filterDTO.setPriorityId(1);

        when(taskService.getTasksByFilter(any(TaskFilterDTO.class), eq(userId))).thenReturn(taskDTOList);

        mockMvc.perform(post("/api/tasks/filter")
                        .with(SecurityMockMvcRequestPostProcessors.user(userDetails))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filterDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].title", is("Test Task")));
    }

    @Test
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000", roles = {"ADMIN"})
    void getMyTasks_ShouldReturnUserTasks() throws Exception {

        when(taskService.getUserActiveTasks(userId)).thenReturn(taskDTOList);

        mockMvc.perform(get("/api/tasks/my-tasks")
                        .with(SecurityMockMvcRequestPostProcessors.user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].title", is("Test Task")));
    }

    @Test
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000", roles = {"ADMIN"})
    void getAllTasks_ShouldReturnTasks() throws Exception {

        when(taskService.getAllTasks(userId)).thenReturn(taskDTOList);

        mockMvc.perform(get("/api/tasks")
                        .with(SecurityMockMvcRequestPostProcessors.user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].title", is("Test Task")));
    }

    @Test
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000", roles = {"ADMIN"})
    void getTaskById_ShouldReturnTask_WhenTaskExists() throws Exception {

        when(taskService.getTaskById(1, userId)).thenReturn(taskDTO);

        mockMvc.perform(get("/api/tasks/1")
                        .with(SecurityMockMvcRequestPostProcessors.user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Test Task")));
    }
}