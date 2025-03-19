package com.taskmanagement.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.UUID;

@Setter
@Getter
public class EpicDTO {
    private Integer id;

    @NotBlank(message = "Epic name is required")
    private String name;

    @NotBlank(message = "Description is required")
    private String description;

    private UUID ownerId;

    @NotBlank(message = "Owner name is required")
    private String ownerName;

    @Min(value = 0, message = "Story points must be a positive number")
    private Integer storyPoints;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private ZonedDateTime startDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private ZonedDateTime targetEndDate;

    public EpicDTO() {
    }

    public EpicDTO(Integer id, String name, String description, String ownerName) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.ownerName = ownerName;
    }
}
