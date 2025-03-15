package com.taskmanagement.dto;

import lombok.Data;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
public class EpicDTO {
    private int id;
    private String name;
    private String description;
    private UUID ownerId;
    private int storyPoints;
    private ZonedDateTime startDate;
    private ZonedDateTime targetEndDate;
}
