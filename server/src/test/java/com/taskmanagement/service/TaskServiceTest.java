package com.taskmanagement.service;

import com.taskmanagement.dto.TaskDTO;
import com.taskmanagement.dto.TaskFilterDTO;
import com.taskmanagement.exception.ResourceNotFound;
import com.taskmanagement.exception.UnauthorizedAccessException;
import com.taskmanagement.model.*;
import com.taskmanagement.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EpicRepository epicRepository;

    @Mock
    private SprintRepository sprintRepository;

    @Mock
    private TaskStatusRepository statusRepository;

    @Mock
    private TaskPriorityRepository priorityRepository;

    @Mock
    private TaskCustomRepository taskCustomRepository;

    @InjectMocks
    private TaskService taskService;

    private UUID userId;
    private UUID assigneeId;
    private Integer epicId;
    private Integer sprintId;
    private Integer taskId;
    private Integer statusId;
    private Integer priorityId;

    private User user;
    private User assignee;
    private Task task;
    private Epic epic;
    private Sprint sprint;
    private TaskStatus status;
    private TaskStatus doneStatus;
    private TaskPriority priority;
    private Role adminRole;
    private Role devRole;

    @BeforeEach
    void setUp() {

        Random rand = new Random();

        userId = UUID.randomUUID();
        assigneeId = UUID.randomUUID();
        epicId = rand.nextInt();
        sprintId = rand.nextInt();
        taskId = 1;
        statusId = 1;
        priorityId = 1;

        adminRole = new Role();
        adminRole.setId(1);
        adminRole.setName("ADMIN");

        devRole = new Role();
        devRole.setId(2);
        devRole.setName("DEVELOPER");

        user = new User();
        user.setId(userId);
        user.setName("Test User");
        user.setEmail("user@example.com");
        user.setRole(adminRole);
        user.setIsActive(true);

        assignee = new User();
        assignee.setId(assigneeId);
        assignee.setName("Test Assignee");
        assignee.setEmail("assignee@example.com");
        assignee.setRole(devRole);
        assignee.setIsActive(true);

        status = new TaskStatus();
        status.setId(statusId);
        status.setName("IN_PROGRESS");

        doneStatus = new TaskStatus();
        doneStatus.setId(5);
        doneStatus.setName("DONE");

        priority = new TaskPriority();
        priority.setId(priorityId);
        priority.setName("HIGH");
        priority.setValue(3);

        epic = new Epic();
        epic.setId(epicId);
        epic.setName("Test Epic");
        epic.setOwner(user);

        sprint = new Sprint();
        sprint.setId(sprintId);
        sprint.setName("Test Sprint");
        sprint.setScrumMaster(user);
        sprint.setActive(true);

        task = new Task();
        task.setId(taskId);
        task.setTitle("Test Task");
        task.setDescription("Test Description");
        task.setCreatedBy(user);
        task.setAssignedTo(assignee);
        task.setStatus(status);
        task.setPriority(priority);
        task.setStoryPoints(5);
        task.setEstimatedHours(8);
        task.setDueDate(ZonedDateTime.now().plusDays(7));
        task.setEpic(epic);
        task.setSprint(sprint);
    }

    @Test
    void getAllTasks_ShouldReturnAllTasks() {
        when(taskRepository.findAll()).thenReturn(Arrays.asList(task));

        List<TaskDTO> tasks = taskService.getAllTasks(userId);

        assertEquals(1, tasks.size());
        assertEquals(taskId, tasks.get(0).getId());
        assertEquals("Test Task", tasks.get(0).getTitle());
        verify(taskRepository, times(1)).findAll();
    }

    @Test
    void getTaskById_ShouldReturnTask_WhenTaskExists() {
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        TaskDTO result = taskService.getTaskById(taskId, userId);

        assertNotNull(result);
        assertEquals(taskId, result.getId());
        assertEquals("Test Task", result.getTitle());
        assertEquals(assigneeId, result.getAssignedToId());
        verify(taskRepository, times(1)).findById(taskId);
    }

    @Test
    void getTaskById_ShouldThrowException_WhenTaskDoesNotExist() {
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        ResourceNotFound exception = assertThrows(ResourceNotFound.class, () -> {
            taskService.getTaskById(taskId, userId);
        });

        assertEquals("Task not found with id: " + taskId, exception.getMessage());
        verify(taskRepository, times(1)).findById(taskId);
    }

    @Test
    void createTask_ShouldCreateTask_WhenAllDataIsValid() {
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setTitle("New Task");
        taskDTO.setDescription("New Description");
        taskDTO.setAssignedToId(assigneeId);
        taskDTO.setStatusId(statusId);
        taskDTO.setPriorityId(priorityId);
        taskDTO.setStoryPoints(3);
        taskDTO.setEstimatedHours(5);
        taskDTO.setDueDate(ZonedDateTime.now().plusDays(7));
        taskDTO.setEpicId(epicId);
        taskDTO.setSprintId(sprintId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findById(assigneeId)).thenReturn(Optional.of(assignee));
        when(statusRepository.findById(statusId)).thenReturn(Optional.of(status));
        when(priorityRepository.findById(priorityId)).thenReturn(Optional.of(priority));
        when(epicRepository.findById(epicId)).thenReturn(Optional.of(epic));
        when(sprintRepository.findById(sprintId)).thenReturn(Optional.of(sprint));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task savedTask = invocation.getArgument(0);
            savedTask.setId(taskId);
            return savedTask;
        });

        TaskDTO result = taskService.createTask(taskDTO, userId);

        assertNotNull(result);
        assertEquals(taskId, result.getId());
        assertEquals("New Task", result.getTitle());
        assertEquals(assigneeId, result.getAssignedToId());
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void createTask_ShouldThrowException_WhenUserNotFound() {
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setTitle("New Task");
        taskDTO.setDescription("New Description");
        taskDTO.setAssignedToId(assigneeId);
        taskDTO.setStatusId(statusId);
        taskDTO.setPriorityId(priorityId);
        taskDTO.setDueDate(ZonedDateTime.now().plusDays(7));

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ResourceNotFound exception = assertThrows(ResourceNotFound.class, () -> {
            taskService.createTask(taskDTO, userId);
        });

        assertEquals("User not found with id: " + userId, exception.getMessage());
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void updateTask_ShouldUpdateTask_WhenUserIsAuthorized() {
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setId(taskId);
        taskDTO.setTitle("Updated Task");
        taskDTO.setDescription("Updated Description");
        taskDTO.setAssignedToId(assigneeId);
        taskDTO.setStatusId(statusId);
        taskDTO.setPriorityId(priorityId);
        taskDTO.setStoryPoints(4);
        taskDTO.setEstimatedHours(6);
        taskDTO.setDueDate(ZonedDateTime.now().plusDays(14));
        taskDTO.setEpicId(epicId);
        taskDTO.setSprintId(sprintId);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findById(assigneeId)).thenReturn(Optional.of(assignee));
        when(statusRepository.findById(statusId)).thenReturn(Optional.of(status));
        when(priorityRepository.findById(priorityId)).thenReturn(Optional.of(priority));
        when(epicRepository.findById(epicId)).thenReturn(Optional.of(epic));
        when(sprintRepository.findById(sprintId)).thenReturn(Optional.of(sprint));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        TaskDTO result = taskService.updateTask(taskDTO, userId);

        assertNotNull(result);
        assertEquals(taskId, result.getId());
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void updateTask_ShouldThrowException_WhenUserIsNotAuthorized() {
        UUID anotherUserId = UUID.randomUUID();
        User anotherUser = new User();
        anotherUser.setId(anotherUserId);
        anotherUser.setName("Another User");
        anotherUser.setRole(devRole);

        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setId(taskId);
        taskDTO.setTitle("Updated Task");
        taskDTO.setAssignedToId(assigneeId);
        taskDTO.setStatusId(statusId);
        taskDTO.setPriorityId(priorityId);
        taskDTO.setDueDate(ZonedDateTime.now().plusDays(14));

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userRepository.findById(anotherUserId)).thenReturn(Optional.of(anotherUser));

        UnauthorizedAccessException exception = assertThrows(UnauthorizedAccessException.class, () -> {
            taskService.updateTask(taskDTO, anotherUserId);
        });

        assertEquals("You don't have permission to update this task", exception.getMessage());
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void changeTaskStatus_ShouldUpdateStatus_WhenUserIsAuthorized() {
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(statusRepository.findById(5)).thenReturn(Optional.of(doneStatus));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        TaskDTO result = taskService.changeTaskStatus(taskId, 5, userId);

        assertNotNull(result);
        assertEquals(taskId, result.getId());
        assertEquals("DONE", result.getStatusName());
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void changeTaskStatus_ShouldSetCompletedAt_WhenStatusIsDone() {
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(statusRepository.findById(5)).thenReturn(Optional.of(doneStatus));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task savedTask = invocation.getArgument(0);
            assertNotNull(savedTask.getCompletedAt());
            savedTask.setStatus(doneStatus);
            return savedTask;
        });

        TaskDTO result = taskService.changeTaskStatus(taskId, 5, userId);

        assertEquals("DONE", result.getStatusName());
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void assignTask_ShouldUpdateAssignee_WhenUserIsAuthorized() {
        UUID newAssigneeId = UUID.randomUUID();
        User newAssignee = new User();
        newAssignee.setId(newAssigneeId);
        newAssignee.setName("New Assignee");
        newAssignee.setRole(devRole);
        newAssignee.setIsActive(true);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findById(newAssigneeId)).thenReturn(Optional.of(newAssignee));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task savedTask = invocation.getArgument(0);
            savedTask.setAssignedTo(newAssignee);
            return savedTask;
        });

        TaskDTO result = taskService.assignTask(taskId, newAssigneeId, userId);

        assertNotNull(result);
        assertEquals(taskId, result.getId());
        assertEquals(newAssigneeId, result.getAssignedToId());
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void addTaskToSprint_ShouldAddTaskToSprint_WhenSprintIsActive() {
        Task taskWithoutSprint = new Task();
        taskWithoutSprint.setId(taskId);
        taskWithoutSprint.setTitle("Test Task");
        taskWithoutSprint.setCreatedBy(user);
        taskWithoutSprint.setAssignedTo(assignee);
        taskWithoutSprint.setStatus(status);
        taskWithoutSprint.setPriority(priority);
        taskWithoutSprint.setDueDate(ZonedDateTime.now().plusDays(7));

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(taskWithoutSprint));
        when(sprintRepository.findById(sprintId)).thenReturn(Optional.of(sprint));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task savedTask = invocation.getArgument(0);
            savedTask.setSprint(sprint);
            return savedTask;
        });

        TaskDTO result = taskService.addTaskToSprint(taskId, sprintId, userId);

        assertNotNull(result);
        assertEquals(taskId, result.getId());
        assertEquals(sprintId, result.getSprintId());
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void addTaskToSprint_ShouldThrowException_WhenSprintIsNotActive() {
        Sprint inactiveSprint = new Sprint();
        inactiveSprint.setId(sprintId);
        inactiveSprint.setName("Inactive Sprint");
        inactiveSprint.setActive(false);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(sprintRepository.findById(sprintId)).thenReturn(Optional.of(inactiveSprint));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            taskService.addTaskToSprint(taskId, sprintId, userId);
        });

        assertEquals("Cannot add tasks to inactive sprints", exception.getMessage());
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void removeTaskFromSprint_ShouldRemoveTaskFromSprint() {
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task savedTask = invocation.getArgument(0);
            savedTask.setSprint(null);
            return savedTask;
        });

        TaskDTO result = taskService.removeTaskFromSprint(taskId, userId);

        assertNotNull(result);
        assertEquals(taskId, result.getId());
        assertNull(result.getSprintId());
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void removeTaskFromSprint_ShouldThrowException_WhenTaskNotInSprint() {
        Task taskWithoutSprint = new Task();
        taskWithoutSprint.setId(taskId);
        taskWithoutSprint.setTitle("Test Task");
        taskWithoutSprint.setCreatedBy(user);
        taskWithoutSprint.setAssignedTo(assignee);
        taskWithoutSprint.setStatus(status);
        taskWithoutSprint.setPriority(priority);
        taskWithoutSprint.setDueDate(ZonedDateTime.now().plusDays(7));

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(taskWithoutSprint));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            taskService.removeTaskFromSprint(taskId, userId);
        });

        assertEquals("Task is not assigned to any sprint", exception.getMessage());
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void addTaskToEpic_ShouldAddTaskToEpic() {
        Task taskWithoutEpic = new Task();
        taskWithoutEpic.setId(taskId);
        taskWithoutEpic.setTitle("Test Task");
        taskWithoutEpic.setCreatedBy(user);
        taskWithoutEpic.setAssignedTo(assignee);
        taskWithoutEpic.setStatus(status);
        taskWithoutEpic.setPriority(priority);
        taskWithoutEpic.setDueDate(ZonedDateTime.now().plusDays(7));

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(taskWithoutEpic));
        when(epicRepository.findById(epicId)).thenReturn(Optional.of(epic));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task savedTask = invocation.getArgument(0);
            savedTask.setEpic(epic);
            return savedTask;
        });

        TaskDTO result = taskService.addTaskToEpic(taskId, epicId, userId);

        assertNotNull(result);
        assertEquals(taskId, result.getId());
        assertEquals(epicId, result.getEpicId());
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void removeTaskFromEpic_ShouldRemoveTaskFromEpic() {
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task savedTask = invocation.getArgument(0);
            savedTask.setEpic(null);
            return savedTask;
        });

        TaskDTO result = taskService.removeTaskFromEpic(taskId, userId);

        assertNotNull(result);
        assertEquals(taskId, result.getId());
        assertNull(result.getEpicId());
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void removeTaskFromEpic_ShouldThrowException_WhenTaskNotInEpic() {
        Task taskWithoutEpic = new Task();
        taskWithoutEpic.setId(taskId);
        taskWithoutEpic.setTitle("Test Task");
        taskWithoutEpic.setCreatedBy(user);
        taskWithoutEpic.setAssignedTo(assignee);
        taskWithoutEpic.setStatus(status);
        taskWithoutEpic.setPriority(priority);
        taskWithoutEpic.setDueDate(ZonedDateTime.now().plusDays(7));

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(taskWithoutEpic));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            taskService.removeTaskFromEpic(taskId, userId);
        });

        assertEquals("Task is not assigned to any epic", exception.getMessage());
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void deleteTask_ShouldDeleteTask_WhenUserIsAdmin() {
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        taskService.deleteTask(taskId, userId);

        verify(taskRepository, times(1)).deleteById(taskId);
    }

    @Test
    void deleteTask_ShouldThrowException_WhenUserIsNotAuthorized() {
        UUID anotherUserId = UUID.randomUUID();
        User anotherUser = new User();
        anotherUser.setId(anotherUserId);
        anotherUser.setName("Another User");
        anotherUser.setRole(devRole);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userRepository.findById(anotherUserId)).thenReturn(Optional.of(anotherUser));

        UnauthorizedAccessException exception = assertThrows(UnauthorizedAccessException.class, () -> {
            taskService.deleteTask(taskId, anotherUserId);
        });

        assertEquals("You don't have permission to delete this task", exception.getMessage());
        verify(taskRepository, never()).deleteById(anyInt());
    }

    @Test
    void getTasksByFilter_ShouldReturnFilteredTasks() {
        TaskFilterDTO filterDTO = new TaskFilterDTO();
        filterDTO.setAssignedToId(assigneeId);
        filterDTO.setStatusId(statusId);
        filterDTO.setPriorityId(priorityId);

        when(taskCustomRepository.findTasksByFilters(
                eq(assigneeId), eq(statusId), eq(priorityId), eq(null), eq(null)))
                .thenReturn(Arrays.asList(task));

        List<TaskDTO> results = taskService.getTasksByFilter(filterDTO, userId);

        assertEquals(1, results.size());
        assertEquals(taskId, results.get(0).getId());
        verify(taskCustomRepository, times(1)).findTasksByFilters(
                eq(assigneeId), eq(statusId), eq(priorityId), eq(null), eq(null));
    }

    @Test
    void getUserActiveTasks_ShouldReturnUserActiveTasks() {
        when(taskRepository.findUserActiveTasks(userId))
                .thenReturn(Arrays.asList(task));

        List<TaskDTO> results = taskService.getUserActiveTasks(userId);

        assertEquals(1, results.size());
        assertEquals(taskId, results.get(0).getId());
        verify(taskRepository, times(1)).findUserActiveTasks(userId);
    }

    @Test
    void addTaskToSprint_ShouldThrowException_WhenTaskDueDateAfterSprintEndDate() {
        Sprint sprint = new Sprint();
        sprint.setId(sprintId);
        sprint.setName("Test Sprint");
        sprint.setScrumMaster(user);
        sprint.setActive(true);
        sprint.setStartDate(ZonedDateTime.now().minusDays(10));
        sprint.setEndDate(ZonedDateTime.now().plusDays(20));
        sprint.setCapacityPoints(100);

        Task taskWithLateDueDate = new Task();
        taskWithLateDueDate.setId(taskId);
        taskWithLateDueDate.setTitle("Late Task");
        taskWithLateDueDate.setCreatedBy(user);
        taskWithLateDueDate.setAssignedTo(assignee);
        taskWithLateDueDate.setStatus(status);
        taskWithLateDueDate.setPriority(priority);
        taskWithLateDueDate.setDueDate(ZonedDateTime.now().plusDays(30));

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(taskWithLateDueDate));
        when(sprintRepository.findById(sprintId)).thenReturn(Optional.of(sprint));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            taskService.addTaskToSprint(taskId, sprintId, userId);
        });

        assertTrue(exception.getMessage().contains("Task due date"));
        assertTrue(exception.getMessage().contains("is after sprint end date"));
    }

    @Test
    void addTaskToSprint_ShouldThrowException_WhenSprintIsCompleted() {
        Sprint completedSprint = new Sprint();
        completedSprint.setId(sprintId);
        completedSprint.setName("Completed Sprint");
        completedSprint.setScrumMaster(user);
        completedSprint.setActive(false);
        completedSprint.setStartDate(ZonedDateTime.now().minusDays(30));
        completedSprint.setEndDate(ZonedDateTime.now().minusDays(10));
        completedSprint.setCapacityPoints(100);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(sprintRepository.findById(sprintId)).thenReturn(Optional.of(completedSprint));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            taskService.addTaskToSprint(taskId, sprintId, userId);
        });

        assertTrue(exception.getMessage().contains("inactive sprint"),
                "Error message should indicate sprint is inactive");
    }

    @Test
    void addTaskToEpic_ShouldThrowException_WhenTaskDueDateAfterEpicEndDate() {
        Epic epicWithEndDate = new Epic();
        epicWithEndDate.setId(epicId);
        epicWithEndDate.setName("Test Epic");
        epicWithEndDate.setOwner(user);
        epicWithEndDate.setStoryPoints(100);
        epicWithEndDate.setStartDate(ZonedDateTime.now().minusDays(10));
        epicWithEndDate.setTargetEndDate(ZonedDateTime.now().plusDays(20));

        Task taskWithLateDueDate = new Task();
        taskWithLateDueDate.setId(taskId);
        taskWithLateDueDate.setTitle("Late Task");
        taskWithLateDueDate.setCreatedBy(user);
        taskWithLateDueDate.setAssignedTo(assignee);
        taskWithLateDueDate.setStatus(status);
        taskWithLateDueDate.setPriority(priority);
        taskWithLateDueDate.setDueDate(ZonedDateTime.now().plusDays(30));

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(taskWithLateDueDate));
        when(epicRepository.findById(epicId)).thenReturn(Optional.of(epicWithEndDate));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            taskService.addTaskToEpic(taskId, epicId, userId);
        });

        assertTrue(exception.getMessage().contains("Task due date"));
        assertTrue(exception.getMessage().contains("is after epic target end date"));
    }

    @Test
    void addTaskToEpic_ShouldThrowException_WhenEpicIsCompleted() {
        Epic completedEpic = new Epic();
        completedEpic.setId(epicId);
        completedEpic.setName("Completed Epic");
        completedEpic.setOwner(user);
        completedEpic.setStoryPoints(100);
        completedEpic.setStartDate(ZonedDateTime.now().minusDays(60));
        completedEpic.setTargetEndDate(ZonedDateTime.now().minusDays(10));
        completedEpic.setActualEndDate(ZonedDateTime.now().minusDays(5));

        when(taskRepository.findByEpicId(epicId)).thenReturn(Collections.emptyList());

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(epicRepository.findById(epicId)).thenReturn(Optional.of(completedEpic));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            taskService.addTaskToEpic(taskId, epicId, userId);
        });

        assertTrue(
                exception.getMessage().toLowerCase().contains("epic"),
                "Error message should indicate the epic is completed"
        );
    }
}