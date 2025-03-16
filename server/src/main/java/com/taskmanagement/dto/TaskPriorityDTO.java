package com.taskmanagement.dto;

import lombok.Data;

@Data
public class TaskPriorityDTO {
    private Integer id;
    private String name;
    private int value;
}