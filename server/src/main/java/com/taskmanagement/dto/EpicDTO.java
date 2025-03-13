package com.taskmanagement.dto;

import java.time.ZonedDateTime;
import java.util.UUID;

public class EpicDTO {
    private String name;
    private String description;
    private UUID ownerId;
    private int storyPoints;
    private ZonedDateTime startDate;
    private ZonedDateTime targetEndDate;

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public UUID getOwnerId() { return ownerId; }
    public void setOwnerId(UUID ownerId) { this.ownerId = ownerId; }

    public int getStoryPoints() { return storyPoints; }
    public void setStoryPoints(int storyPoints) { this.storyPoints = storyPoints; }

    public ZonedDateTime getStartDate() { return startDate; }
    public void setStartDate(ZonedDateTime startDate) { this.startDate = startDate; }

    public ZonedDateTime getTargetEndDate() { return targetEndDate; }
    public void setTargetEndDate(ZonedDateTime targetEndDate) { this.targetEndDate = targetEndDate; }
}
