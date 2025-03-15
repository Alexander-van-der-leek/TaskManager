package com.taskmanagement.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.taskmanagement.model.TaskComment;

@Repository
public interface TaskCommentRepository extends JpaRepository<TaskComment, Integer> {
    List<TaskComment> findByTaskId(Integer taskId);
    List<TaskComment> findByTaskIdOrderByCreatedAtDesc(Integer taskId);
    List<TaskComment> findByUserId(UUID userId);
}