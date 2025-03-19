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
    List<Task> findByAssignedTo(User assignedTo);

    List<Task> findByCreatedBy(User createdBy);

    @Query("SELECT t FROM Task t JOIN t.status s WHERE s.name = :statusName")
    List<Task> findByStatusName(@Param("statusName") String statusName);

    List<Task> findByEpicId(Integer epicId);

    List<Task> findBySprintId(Integer sprintId);

    List<Task> findByDueDateBeforeAndCompletedAtIsNull(ZonedDateTime now);

    List<Task> findByUpdatedAtAfterOrderByUpdatedAtDesc(ZonedDateTime since);

    // get tasks for a user
    @Query("SELECT t FROM Task t WHERE t.assignedTo.id = :userId AND (t.status.name != 'DONE' OR t.completedAt IS NULL)")
    List<Task> findUserActiveTasks(@Param("userId") UUID userId);

    // get counts for tasks in sprint with status
    @Query("SELECT COUNT(t) FROM Task t WHERE t.sprint.id = :sprintId AND t.status.id = :statusId")
    long countTasksBySprintAndStatus(@Param("sprintId") Integer sprintId, @Param("statusId") Integer statusId);

    // get by name
    @Query("SELECT t FROM Task t WHERE LOWER(t.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    List<Task> findByTitleContainingIgnoreCase(@Param("title") String title);
}