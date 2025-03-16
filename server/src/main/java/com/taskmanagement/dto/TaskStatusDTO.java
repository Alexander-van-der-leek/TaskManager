package com.taskmanagement.dto;

import lombok.Data;

@Data
public class TaskStatusDTO {
    private Integer id;
    private String name;
    private int displayOrder;
}
