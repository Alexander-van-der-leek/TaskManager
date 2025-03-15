package com.taskmanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
public class TaskCommentDTO {
    private Integer id;

    private Integer taskId;

    private UUID userId;

    private String userName;

    @NotBlank(message = "Comment content is required")
    private String content;

    private ZonedDateTime createdAt;

    private ZonedDateTime updatedAt;

}