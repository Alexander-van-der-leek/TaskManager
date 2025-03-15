package com.taskmanagement.service;

import com.taskmanagement.dto.TaskCommentDTO;
import com.taskmanagement.exception.ResourceNotFound;
import com.taskmanagement.exception.UnauthorizedAccessException;
import com.taskmanagement.model.Task;
import com.taskmanagement.model.TaskComment;
import com.taskmanagement.model.User;
import com.taskmanagement.repository.TaskCommentRepository;
import com.taskmanagement.repository.TaskRepository;
import com.taskmanagement.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TaskCommentService {
    private static final Logger logger = LoggerFactory.getLogger(TaskCommentService.class);

    private final TaskCommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskCommentService(
            TaskCommentRepository commentRepository,
            TaskRepository taskRepository,
            UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<TaskCommentDTO> getCommentsByTaskId(Integer taskId, UUID userId) {
        logger.debug("Fetching comments for task: {}", taskId);

        if (!taskRepository.existsById(taskId)) {
            throw new ResourceNotFound("Task not found with id: " + taskId);
        }

        return commentRepository.findByTaskIdOrderByCreatedAtDesc(taskId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public TaskCommentDTO addComment(TaskCommentDTO commentDTO, UUID creatorId) {
        logger.debug("Adding comment to task {} by user {}", commentDTO.getTaskId(), creatorId);

        Task task = taskRepository.findById(commentDTO.getTaskId())
                .orElseThrow(() -> new ResourceNotFound("Task not found with id: " + commentDTO.getTaskId()));

        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new ResourceNotFound("User not found with id: " + creatorId));

        TaskComment comment = new TaskComment();
        comment.setTask(task);
        comment.setUser(creator);
        comment.setContent(commentDTO.getContent());

        TaskComment savedComment = commentRepository.save(comment);
        logger.info("Added comment with ID: {} to task: {}", savedComment.getId(), task.getId());

        return convertToDTO(savedComment);
    }

    @Transactional
    public TaskCommentDTO updateComment(Integer commentId, TaskCommentDTO commentDTO, UUID userId) {
        logger.debug("Updating comment {} by user {}", commentId, userId);

        TaskComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFound("Comment not found with id: " + commentId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFound("User not found with id: " + userId));

        boolean isAdmin = user.getRole().getName().equals("ADMIN");
        boolean isAuthor = comment.getUser().getId().equals(userId);

        if (!(isAdmin || isAuthor)) {
            throw new UnauthorizedAccessException("You don't have permission to update this comment");
        }

        comment.setContent(commentDTO.getContent());
        TaskComment updatedComment = commentRepository.save(comment);
        logger.info("Updated comment with ID: {}", updatedComment.getId());

        return convertToDTO(updatedComment);
    }

    @Transactional
    public void deleteComment(Integer commentId, UUID userId) {
        logger.debug("Deleting comment {} by user {}", commentId, userId);

        TaskComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFound("Comment not found with id: " + commentId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFound("User not found with id: " + userId));

        boolean isAdmin = user.getRole().getName().equals("ADMIN");
        boolean isAuthor = comment.getUser().getId().equals(userId);

        if (!(isAdmin || isAuthor)) {
            throw new UnauthorizedAccessException("You don't have permission to delete this comment");
        }

        commentRepository.deleteById(commentId);
        logger.info("Deleted comment with ID: {}", commentId);
    }

    private TaskCommentDTO convertToDTO(TaskComment comment) {
        TaskCommentDTO dto = new TaskCommentDTO();
        dto.setId(comment.getId());
        dto.setTaskId(comment.getTask().getId());
        dto.setUserId(comment.getUser().getId());
        dto.setUserName(comment.getUser().getName());
        dto.setContent(comment.getContent());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setUpdatedAt(comment.getUpdatedAt());
        return dto;
    }
}