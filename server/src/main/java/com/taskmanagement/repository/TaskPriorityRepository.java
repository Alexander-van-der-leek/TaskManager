package com.taskmanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.taskmanagement.model.TaskPriority;

@Repository
public interface TaskPriorityRepository extends JpaRepository<TaskPriority, Integer> {
}