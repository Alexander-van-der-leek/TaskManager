package com.taskmanagement.controller;

import com.taskmanagement.dto.TaskCommentDTO;
import com.taskmanagement.service.TaskCommentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/comments")
public class TaskCommentController {

    private static final Logger logger = LoggerFactory.getLogger(TaskCommentController.class);

    private final TaskCommentService commentService;

    public TaskCommentController(TaskCommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/task/{taskId}")
    public ResponseEntity<List<TaskCommentDTO>> getCommentsByTaskId(
            @PathVariable Integer taskId,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        logger.info("User {} requesting comments for task {}", userId, taskId);
        return ResponseEntity.ok(commentService.getCommentsByTaskId(taskId, userId));
    }

    @PostMapping
    public ResponseEntity<TaskCommentDTO> addComment(
            @Valid @RequestBody TaskCommentDTO commentDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        logger.info("User {} adding comment to task {}", userId, commentDTO.getTaskId());
        return ResponseEntity.ok(commentService.addComment(commentDTO, userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskCommentDTO> updateComment(
            @PathVariable Integer id,
            @Valid @RequestBody TaskCommentDTO commentDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        logger.info("User {} updating comment {}", userId, id);
        return ResponseEntity.ok(commentService.updateComment(id, commentDTO, userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        logger.info("User {} deleting comment {}", userId, id);
        commentService.deleteComment(id, userId);
        return ResponseEntity.noContent().build();
    }
}