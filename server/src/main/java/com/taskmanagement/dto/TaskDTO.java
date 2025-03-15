package com.taskmanagement.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
public class TaskDTO {
    private Integer id;

    private Integer epicId;

    private Integer sprintId;

    private UUID createdById;

    @NotNull(message = "Assigned user ID is required")
    private UUID assignedToId;

    @NotNull(message = "Priority ID is required")
    private Integer priorityId;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Status ID is required")
    private Integer statusId;

    @Min(value = 0, message = "Story points must be a positive number")
    private int storyPoints;

    @Min(value = 0, message = "Estimated hours must be a positive number")
    private int estimatedHours;

    @NotNull(message = "Due date is required")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private ZonedDateTime dueDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private ZonedDateTime completedAt;

    private String assignedToName;
    private String statusName;
    private String priorityName;
    private String epicName;
    private String sprintName;

}