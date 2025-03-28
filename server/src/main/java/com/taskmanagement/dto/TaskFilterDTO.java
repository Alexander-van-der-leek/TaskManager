package com.taskmanagement.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class TaskFilterDTO {
    private UUID assignedToId;
    private Integer statusId;
    private Integer priorityId;
    private Integer sprintId;
    private Integer epicId;

    public TaskFilterDTO() {
    }

    public TaskFilterDTO(UUID assignedToId, Integer statusId, Integer priorityId, Integer sprintId, Integer epicId) {
        this.assignedToId = assignedToId;
        this.statusId = statusId;
        this.priorityId = priorityId;
        this.sprintId = sprintId;
        this.epicId = epicId;
    }
}