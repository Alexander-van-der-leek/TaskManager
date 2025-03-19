package com.taskmanagement.service;

import com.taskmanagement.dto.TaskCommentDTO;
import com.taskmanagement.exception.ResourceNotFound;
import com.taskmanagement.exception.UnauthorizedAccessException;
import com.taskmanagement.model.Role;
import com.taskmanagement.model.Task;
import com.taskmanagement.model.TaskComment;
import com.taskmanagement.model.User;
import com.taskmanagement.repository.TaskCommentRepository;
import com.taskmanagement.repository.TaskRepository;
import com.taskmanagement.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskCommentServiceTest {

    @Mock
    private TaskCommentRepository commentRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskCommentService commentService;

    private UUID userId;
    private UUID otherUserId;
    private Integer taskId;
    private Integer commentId;
    private User user;
    private User otherUser;
    private User adminUser;
    private Task task;
    private TaskComment comment;
    private Role userRole;
    private Role adminRole;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        otherUserId = UUID.randomUUID();
        taskId = 1;
        commentId = 1;

        userRole = new Role();
        userRole.setId(2);
        userRole.setName("DEVELOPER");

        adminRole = new Role();
        adminRole.setId(1);
        adminRole.setName("ADMIN");

        user = new User();
        user.setId(userId);
        user.setName("Test User");
        user.setEmail("user@example.com");
        user.setRole(userRole);

        otherUser = new User();
        otherUser.setId(otherUserId);
        otherUser.setName("Other User");
        otherUser.setEmail("other@example.com");
        otherUser.setRole(userRole);

        adminUser = new User();
        adminUser.setId(UUID.randomUUID());
        adminUser.setName("Admin User");
        adminUser.setEmail("admin@example.com");
        adminUser.setRole(adminRole);

        task = new Task();
        task.setId(taskId);
        task.setTitle("Test Task");

        comment = new TaskComment();
        comment.setId(commentId);
        comment.setTask(task);
        comment.setUser(user);
        comment.setContent("Test Comment");
        comment.setCreatedAt(ZonedDateTime.now());
        comment.setUpdatedAt(ZonedDateTime.now());
    }

    @Test
    void getCommentsByTaskId_ShouldReturnComments_WhenTaskExists() {
        when(taskRepository.existsById(taskId)).thenReturn(true);
        when(commentRepository.findByTaskIdOrderByCreatedAtDesc(taskId)).thenReturn(Arrays.asList(comment));

        List<TaskCommentDTO> comments = commentService.getCommentsByTaskId(taskId, userId);

        assertEquals(1, comments.size());
        assertEquals(commentId, comments.get(0).getId());
        assertEquals("Test Comment", comments.get(0).getContent());
        verify(commentRepository, times(1)).findByTaskIdOrderByCreatedAtDesc(taskId);
    }

    @Test
    void getCommentsByTaskId_ShouldThrowException_WhenTaskDoesNotExist() {
        when(taskRepository.existsById(taskId)).thenReturn(false);

        ResourceNotFound exception = assertThrows(ResourceNotFound.class, () -> {
            commentService.getCommentsByTaskId(taskId, userId);
        });

        assertEquals("Task not found with id: " + taskId, exception.getMessage());
        verify(commentRepository, never()).findByTaskIdOrderByCreatedAtDesc(taskId);
    }

    @Test
    void addComment_ShouldCreateComment_WhenAllDataIsValid() {
        TaskCommentDTO commentDTO = new TaskCommentDTO();
        commentDTO.setTaskId(taskId);
        commentDTO.setContent("New Comment");

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(commentRepository.save(any(TaskComment.class))).thenAnswer(invocation -> {
            TaskComment savedComment = invocation.getArgument(0);
            savedComment.setId(commentId);
            return savedComment;
        });

        TaskCommentDTO result = commentService.addComment(commentDTO, userId);

        assertNotNull(result);
        assertEquals(commentId, result.getId());
        assertEquals("New Comment", result.getContent());
        assertEquals(taskId, result.getTaskId());
        assertEquals(userId, result.getUserId());
        verify(commentRepository, times(1)).save(any(TaskComment.class));
    }

    @Test
    void addComment_ShouldThrowException_WhenTaskNotFound() {
        TaskCommentDTO commentDTO = new TaskCommentDTO();
        commentDTO.setTaskId(taskId);
        commentDTO.setContent("New Comment");

        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        ResourceNotFound exception = assertThrows(ResourceNotFound.class, () -> {
            commentService.addComment(commentDTO, userId);
        });

        assertEquals("Task not found with id: " + taskId, exception.getMessage());
        verify(commentRepository, never()).save(any(TaskComment.class));
    }

    @Test
    void updateComment_ShouldUpdateComment_WhenUserIsAuthor() {
        TaskCommentDTO commentDTO = new TaskCommentDTO();
        commentDTO.setContent("Updated Comment");

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(commentRepository.save(any(TaskComment.class))).thenReturn(comment);

        TaskCommentDTO result = commentService.updateComment(commentId, commentDTO, userId);

        assertNotNull(result);
        assertEquals(commentId, result.getId());
        assertEquals("Updated Comment", result.getContent());
        verify(commentRepository, times(1)).save(any(TaskComment.class));
    }

    @Test
    void updateComment_ShouldUpdateComment_WhenUserIsAdmin() {
        TaskCommentDTO commentDTO = new TaskCommentDTO();
        commentDTO.setContent("Admin Updated Comment");

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(userRepository.findById(adminUser.getId())).thenReturn(Optional.of(adminUser));
        when(commentRepository.save(any(TaskComment.class))).thenReturn(comment);

        TaskCommentDTO result = commentService.updateComment(commentId, commentDTO, adminUser.getId());

        assertNotNull(result);
        assertEquals(commentId, result.getId());
        assertEquals("Admin Updated Comment", result.getContent());
        verify(commentRepository, times(1)).save(any(TaskComment.class));
    }

    @Test
    void updateComment_ShouldThrowException_WhenUserIsNotAuthorized() {
        TaskCommentDTO commentDTO = new TaskCommentDTO();
        commentDTO.setContent("Unauthorized Update");

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(userRepository.findById(otherUserId)).thenReturn(Optional.of(otherUser));

        UnauthorizedAccessException exception = assertThrows(UnauthorizedAccessException.class, () -> {
            commentService.updateComment(commentId, commentDTO, otherUserId);
        });

        assertEquals("You don't have permission to update this comment", exception.getMessage());
        verify(commentRepository, never()).save(any(TaskComment.class));
    }

    @Test
    void deleteComment_ShouldDeleteComment_WhenUserIsAuthor() {
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        commentService.deleteComment(commentId, userId);

        verify(commentRepository, times(1)).deleteById(commentId);
    }

    @Test
    void deleteComment_ShouldDeleteComment_WhenUserIsAdmin() {
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(userRepository.findById(adminUser.getId())).thenReturn(Optional.of(adminUser));

        commentService.deleteComment(commentId, adminUser.getId());

        verify(commentRepository, times(1)).deleteById(commentId);
    }

    @Test
    void deleteComment_ShouldThrowException_WhenUserIsNotAuthorized() {
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(userRepository.findById(otherUserId)).thenReturn(Optional.of(otherUser));

        UnauthorizedAccessException exception = assertThrows(UnauthorizedAccessException.class, () -> {
            commentService.deleteComment(commentId, otherUserId);
        });

        assertEquals("You don't have permission to delete this comment", exception.getMessage());
        verify(commentRepository, never()).deleteById(commentId);
    }
}