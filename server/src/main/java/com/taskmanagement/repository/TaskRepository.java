package com.taskmanagement.repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.taskmanagement.model.Task;
import com.taskmanagement.model.User;

@Repository
public interface TaskRepository extends JpaRepository<Task, Integer> {
    // Find tasks by assigned user
    List<Task> findByAssignedTo(User assignedTo);

    // Find tasks by creator
    List<Task> findByCreatedBy(User createdBy);

    // Find tasks by status name
    @Query("SELECT t FROM Task t JOIN t.status s WHERE s.name = :statusName")
    List<Task> findByStatusName(@Param("statusName") String statusName);

    // Find tasks by epic id
    List<Task> findByEpicId(UUID epicId);

    // Find tasks by sprint id
    List<Task> findBySprintId(UUID sprintId);

    // Find overdue tasks
    List<Task> findByDueDateBeforeAndCompletedAtIsNull(ZonedDateTime now);

    // Find recently updated tasks
    List<Task> findByUpdatedAtAfterOrderByUpdatedAtDesc(ZonedDateTime since);

    // Find tasks for user dashboard (assigned to them and not completed)
    @Query("SELECT t FROM Task t WHERE t.assignedTo.id = :userId AND (t.status.name != 'DONE' OR t.completedAt IS NULL)")
    List<Task> findUserActiveTasks(@Param("userId") UUID userId);

    // Count tasks by status for sprint
    @Query("SELECT COUNT(t) FROM Task t WHERE t.sprint.id = :sprintId AND t.status.id = :statusId")
    long countTasksBySprintAndStatus(@Param("sprintId") UUID sprintId, @Param("statusId") Integer statusId);
}