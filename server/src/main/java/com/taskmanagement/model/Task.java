package com.taskmanagement.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "tasks")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "epic_id")
    private Epic epic;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sprint_id")
    private Sprint sprint;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assigned_to_id", nullable = false)
    private User assignedTo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "priority_id", nullable = false)
    private TaskPriority priority;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "status_id", nullable = false)
    private TaskStatus status;

    @Column(name = "story_points", nullable = false)
    private int storyPoints;

    @Column(name = "estimated_hours", nullable = false)
    private int estimatedHours;

    @Column(name = "due_date", nullable = false)
    private ZonedDateTime dueDate;

    @Column(name = "completed_at")
    private ZonedDateTime completedAt;

    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = updatedAt = ZonedDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = ZonedDateTime.now();
    }

}