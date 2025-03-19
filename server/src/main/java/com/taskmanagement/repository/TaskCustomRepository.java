package com.taskmanagement.repository;

import com.taskmanagement.model.Task;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// had to add this to get filtering to work instead of via query
@Repository
public class TaskCustomRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public List<Task> findTasksByFilters(UUID assignedToId, Integer statusId, Integer priorityId, Integer sprintId, Integer epicId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Task> query = cb.createQuery(Task.class);
        Root<Task> task = query.from(Task.class);

        List<Predicate> predicates = new ArrayList<>();

        if (assignedToId != null) {
            predicates.add(cb.equal(task.get("assignedTo").get("id"), assignedToId));
        }

        if (statusId != null) {
            predicates.add(cb.equal(task.get("status").get("id"), statusId));
        }

        if (priorityId != null) {
            predicates.add(cb.equal(task.get("priority").get("id"), priorityId));
        }

        if (sprintId != null) {
            predicates.add(cb.equal(task.get("sprint").get("id"), sprintId));
        }

        if (epicId != null) {
            predicates.add(cb.equal(task.get("epic").get("id"), epicId));
        }

        if (!predicates.isEmpty()) {
            query.where(predicates.toArray(new Predicate[0]));
        }

        return entityManager.createQuery(query).getResultList();
    }
}