package com.taskmanagement.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.UUID;

@Setter
@Getter
public class EpicDTO {
    private String name;
    private String description;
    private UUID ownerId;
    private Integer storyPoints;
    private ZonedDateTime startDate;
    private ZonedDateTime targetEndDate;
}
