package com.taskmanagement.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "task_priorities")
public class TaskPriority {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String name;


    @Column(nullable = false)
    private int value;

}