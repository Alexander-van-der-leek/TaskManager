package com.taskmanagement.dto;

import lombok.Data;

import java.time.ZonedDateTime;
import java.util.UUID;
@Data
public class SprintDTO {
    private int id;
    private String name;
    private String goal;
    private UUID scrumMasterId;
    private int capacityPoints;
    private ZonedDateTime startDate;
    private ZonedDateTime endDate;
    private boolean isActive;
}
