package com.taskmanagement.dto;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.UUID;

@Setter
@Getter
public class EpicDTO {
    private Integer id;
    private String name;
    private String description;
    private UUID ownerId;
    private String ownerName;
    @Min(value = 0, message = "Story points must be a positive number")
    private Integer storyPoints;
    private ZonedDateTime startDate;
    private ZonedDateTime targetEndDate;

    public EpicDTO() {
    }

    public EpicDTO(int id, String name, String ownerName) {
        this.id = id;
        this.name = name;
        this.ownerName = ownerName;
    }
}