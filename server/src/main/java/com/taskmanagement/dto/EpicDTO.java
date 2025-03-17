package com.taskmanagement.dto;

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
    private Integer storyPoints;
    private ZonedDateTime startDate;
    private ZonedDateTime targetEndDate;

    public EpicDTO(Integer id, String name, String ownerName) {
        this.id = id;
        this.name = name;
        this.ownerName = ownerName;
    }
}
